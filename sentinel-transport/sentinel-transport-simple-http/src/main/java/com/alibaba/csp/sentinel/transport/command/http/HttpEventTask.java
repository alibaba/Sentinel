/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport.command.http;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.CommandCenterLog;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter;
import com.alibaba.csp.sentinel.transport.command.exception.BadRequestException;
import com.alibaba.csp.sentinel.transport.command.exception.ContentTypeNotSupportedException;
import com.alibaba.csp.sentinel.transport.util.HttpCommandUtils;
import com.alibaba.csp.sentinel.util.StringUtil;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/***
 * The task handles incoming command request in HTTP protocol.
 *
 * @author youji.zj
 * @author Eric Zhao
 */
public class HttpEventTask implements Runnable {
    private static final String SERVER_ERROR_MESSAGE = "Command server error";

    private final Socket socket;

    private boolean writtenHead = false;

    public HttpEventTask(Socket socket) {
        this.socket = socket;
    }

    public void close() throws Exception {
        socket.close();
    }

    @Override
    public void run() {
        if (socket == null) {
            return;
        }

        BufferedReader in = null;
        PrintWriter printWriter = null;
        try {
            long start = System.currentTimeMillis();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), SentinelConfig.charset()));
            OutputStream outputStream = socket.getOutputStream();

            printWriter = new PrintWriter(
                new OutputStreamWriter(outputStream, Charset.forName(SentinelConfig.charset())));

            String firstLine = in.readLine();
            CommandCenterLog.info("[SimpleHttpCommandCenter] Socket income: " + firstLine
                + ", addr: " + socket.getInetAddress());
            CommandRequest request = processQueryString(firstLine);

            if (firstLine.length() > 4 && StringUtil.equalsIgnoreCase("POST", firstLine.substring(0, 4))) {
                // Deal with post method
                processPostRequest(in, request);
            }

            // Validate the target command.
            String commandName = HttpCommandUtils.getTarget(request);
            if (StringUtil.isBlank(commandName)) {
                badRequest(printWriter, "Invalid command");
                return;
            }

            // Find the matching command handler.
            CommandHandler<?> commandHandler = SimpleHttpCommandCenter.getHandler(commandName);
            if (commandHandler != null) {
                CommandResponse<?> response = commandHandler.handle(request);
                handleResponse(response, printWriter, outputStream);
            } else {
                // No matching command handler.
                badRequest(printWriter, "Unknown command `" + commandName + '`');
            }
            printWriter.flush();

            long cost = System.currentTimeMillis() - start;
            CommandCenterLog.info("[SimpleHttpCommandCenter] Deal a socket task: " + firstLine
                + ", address: " + socket.getInetAddress() + ", time cost: " + cost + " ms");
        } catch (BadRequestException e) {
            badRequest(printWriter, "Bad request");
        } catch (ContentTypeNotSupportedException e) {
            badRequest(printWriter, "Content type is not supported");
        } catch (Throwable e) {
            CommandCenterLog.warn("[SimpleHttpCommandCenter] CommandCenter error", e);
            try {
                if (printWriter != null) {
                    String errorMessage = SERVER_ERROR_MESSAGE;
                    if (!writtenHead) {
                        internalError(printWriter, errorMessage);
                    } else {
                        printWriter.println(errorMessage);
                    }
                    printWriter.flush();
                }
            } catch (Exception e1) {
                CommandCenterLog.warn("[SimpleHttpCommandCenter] Close server socket failed", e);
            }
        } finally {
            closeResource(in);
            closeResource(printWriter);
            closeResource(socket);
        }
    }
    
    /**
     * Try to process the body of POST request additionally.
     * 
     * @param bufferedReader
     * @param request
     * @throws IOException
     * @throws BadRequestException
     * @throws ContentTypeNotSupportedException 
     */
    protected static void processPostRequest(BufferedReader bufferedReader, CommandRequest request)
            throws BadRequestException, IOException, ContentTypeNotSupportedException {
        Map<String, String> headerMap = parsePostHeaders(bufferedReader);
        
        if (headerMap == null) {
            // illegal request
            RecordLog.warn("Illegal request read");
            throw new BadRequestException();
        }
        
        if (headerMap.containsKey("content-type") && !checkSupport(headerMap.get("content-type"))) {
            // not support Content-type
            RecordLog.warn("Not supported Content-Type: {}", headerMap.get("content-type"));
            throw new ContentTypeNotSupportedException();
        }
        
        int bodyLength;
        try {
            bodyLength = Integer.parseInt(headerMap.get("content-length"));
        } catch (Exception e) {
            // illegal request without Content-length header
            RecordLog.warn("No available Content-Length in headers");
            throw new BadRequestException();
        }
        
        parseParams(readBody(bufferedReader, bodyLength), request);
    }
    
    /**
     * Process header line in request
     * 
     * @param line
     * @return return headers in a Map, null for illegal request
     * @throws IOException 
     */
    protected static Map<String, String> parsePostHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headerMap = new HashMap<String, String>(4);
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) {
                // no body
                return null;
            }
            if (line.length() == 0) {
                // empty line, body ahead
                return headerMap;
            }
            int index = line.indexOf(":");
            if (index < 1) {
                // empty value, abandon
                continue;
            }
            String headerName = line.substring(0, index).trim().toLowerCase();
            String headerValue = line.substring(index + 1).trim();
            if (headerValue.length() > 0) {
                headerMap.put(headerName, headerValue);
            }
        }
    }
    
    private static boolean checkSupport(String contentType) {
        int idx = contentType.indexOf(";");
        String type;
        if (idx > 0) {
            type = contentType.substring(0, idx).toLowerCase().trim();
        } else {
            type = contentType.toLowerCase();
        }
        // Actually in RFC "x-*" shouldn't have any properties like "type/subtype; key=val"
        // But some library do add it. So we will be compatible with that but force to
        // encoding specified in configuration as legacy processing will do.
        if (!type.contains("application/x-www-form-urlencoded")) {
            CommandCenterLog.warn("Content-Type not supported: " + contentType);
            // Not supported request type
            // Now simple-http only support form-encoded post request.
            return false;
        }
        return true;
    }
    
    private static String readBody(BufferedReader in, int bodyLength) throws IOException {
        int read = 0;
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[512];
        while (read < bodyLength) {
            int l = in.read(buf);
            if (l < 0) {
                break;
            }
            if (l == 0) continue;
            read += l;
            sb.append(buf, 0, l);
        }
        return sb.toString();
    }
    
    /**
     * Consume all the body submitted and parse params into {@link CommandRequest}
     * 
     * @param queryString
     * @param request
     */
    protected static void parseParams(String queryString, CommandRequest request) {
        if (queryString == null || queryString.length() < 1) {
            return;
        }
        
        int offset = 0, pos = -1;
        
        // check anchor
        queryString = removeAnchor(queryString);
        
        while (true) {
            offset = pos + 1;
            pos = queryString.indexOf('&', offset);
            if (offset == pos) {
                // empty
                continue;
            }
            parseSingleParam(queryString.substring(offset, pos == -1 ? queryString.length() : pos), request);

            if (pos < 0) {
                // reach the end
                break;
            }
        }
    }

    private void closeResource(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                CommandCenterLog.warn("[SimpleHttpCommandCenter] Close resource failed", e);
            }
        }
    }

    private <T> void handleResponse(CommandResponse<T> response, /*@NonNull*/ final PrintWriter printWriter,
        /*@NonNull*/ final OutputStream rawOutputStream) throws Exception {
        if (response.isSuccess()) {
            if (response.getResult() == null) {
                writeOkStatusLine(printWriter);
                return;
            }
            // Write 200 OK status line.
            writeOkStatusLine(printWriter);
            // Here we directly use `toString` to encode the result to plain text.
            byte[] buffer = response.getResult().toString().getBytes(SentinelConfig.charset());
            rawOutputStream.write(buffer);
            rawOutputStream.flush();
        } else {
            String msg = SERVER_ERROR_MESSAGE;
            if (response.getException() != null) {
                msg = response.getException().getMessage();
            }
            badRequest(printWriter, msg);
        }
    }

    /**
     * Write `400 Bad Request` HTTP response status line and message body, then flush.
     */
    private void badRequest(/*@NonNull*/ final PrintWriter out, String message) {
        out.print("HTTP/1.1 400 Bad Request\r\n"
            + "Connection: close\r\n\r\n");
        out.print(message);
        out.flush();
        writtenHead = true;
    }

    /**
     * Write `500 Internal Server Error` HTTP response status line and message body, then flush.
     */
    private void internalError(/*@NonNull*/ final PrintWriter out, String message) {
        out.print("HTTP/1.1 500 Internal Server Error\r\n"
            + "Connection: close\r\n\r\n");
        out.print(message);
        out.flush();
        writtenHead = true;
    }

    /**
     * Write `200 OK` HTTP response status line and flush.
     */
    private void writeOkStatusLine(/*@NonNull*/ final PrintWriter out) {
        out.print("HTTP/1.1 200 OK\r\n"
            + "Connection: close\r\n\r\n");
        out.flush();
        writtenHead = true;
    }

    /**
     * Parse raw HTTP request line to a {@link CommandRequest}.
     *
     * @param line HTTP request line
     * @return parsed command request
     */
    protected static CommandRequest processQueryString(String line) {
        CommandRequest request = new CommandRequest();
        if (StringUtil.isBlank(line)) {
            return request;
        }
        int start = line.indexOf('/');
        int ask = line.indexOf('?') == -1 ? line.lastIndexOf(' ') : line.indexOf('?');
        int space = line.lastIndexOf(' ');
        String target = line.substring(start != -1 ? start + 1 : 0, ask != -1 ? ask : line.length());
        request.addMetadata(HttpCommandUtils.REQUEST_TARGET, target);
        if (ask == -1 || ask == space) {
            return request;
        }
        String parameterStr = line.substring(ask != -1 ? ask + 1 : 0, space != -1 ? space : line.length());
        parseParams(parameterStr, request);
        return request;
    }
    
    /**
     * Truncate query from "a=1&b=2#mark" to "a=1&b=2"
     * 
     * @param str
     * @return
     */
    protected static String removeAnchor(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        
        int anchor = str.indexOf('#');
        
        if (anchor == 0) {
            return "";
        } else if (anchor > 0) {
            return str.substring(0, anchor);
        }
        
        return str;
    }
    
    protected static void parseSingleParam(String single, CommandRequest request) {
        if (single == null || single.length() < 3) {
            return;
        }
        
        int index = single.indexOf('=');
        if (index <= 0 || index >= single.length() - 1) {
            // empty key/val or nothing found
            return;
        }

        String value = StringUtil.trim(single.substring(index + 1));
        String key = StringUtil.trim(single.substring(0, index));
        try {
            key = URLDecoder.decode(key, SentinelConfig.charset());
            value = URLDecoder.decode(value, SentinelConfig.charset());
        } catch (UnsupportedEncodingException e) {
        }
        
        request.addParam(key, value);
    }

}
