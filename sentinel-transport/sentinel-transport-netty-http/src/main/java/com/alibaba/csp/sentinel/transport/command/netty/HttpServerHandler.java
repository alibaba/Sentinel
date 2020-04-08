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
package com.alibaba.csp.sentinel.transport.command.netty;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.log.CommandCenterLog;
import com.alibaba.csp.sentinel.transport.command.codec.CodecRegistry;
import com.alibaba.csp.sentinel.transport.command.codec.Encoder;
import com.alibaba.csp.sentinel.transport.util.HttpCommandUtils;
import com.alibaba.csp.sentinel.util.StringUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Netty-based HTTP server handler for command center.
 *
 * Note: HTTP chunked is not tested!
 *
 * @author Eric Zhao
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    private final CodecRegistry codecRegistry = new CodecRegistry();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest httpRequest = (FullHttpRequest)msg;
        try {
            CommandRequest request = parseRequest(httpRequest);
            if (StringUtil.isBlank(HttpCommandUtils.getTarget(request))) {
                writeErrorResponse(BAD_REQUEST.code(), "Invalid command", ctx);
                return;
            }
            handleRequest(request, ctx, HttpUtil.isKeepAlive(httpRequest));

        } catch (Exception ex) {
            writeErrorResponse(INTERNAL_SERVER_ERROR.code(), SERVER_ERROR_MESSAGE, ctx);
            CommandCenterLog.warn("Internal error", ex);
        }
    }

    private void handleRequest(CommandRequest request, ChannelHandlerContext ctx, boolean keepAlive)
        throws Exception {
        String commandName = HttpCommandUtils.getTarget(request);
        // Find the matching command handler.
        CommandHandler<?> commandHandler = getHandler(commandName);
        if (commandHandler != null) {
            CommandResponse<?> response = commandHandler.handle(request);
            writeResponse(response, ctx, keepAlive);
        } else {
            // No matching command handler.
            writeErrorResponse(BAD_REQUEST.code(), String.format("Unknown command \"%s\"", commandName), ctx);
        }
    }

    private Encoder<?> pickEncoder(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Bad class metadata");
        }
        for (Encoder<?> encoder : codecRegistry.getEncoderList()) {
            if (encoder.canEncode(clazz)) {
                return encoder;
            }
        }
        return null;
    }

    private void writeErrorResponse(int statusCode, String message, ChannelHandlerContext ctx) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.valueOf(statusCode),
            Unpooled.copiedBuffer(message, Charset.forName(SentinelConfig.charset())));

        httpResponse.headers().set("Content-Type", "text/plain; charset=" + SentinelConfig.charset());
        ctx.write(httpResponse);

        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    private void writeResponse(CommandResponse response, ChannelHandlerContext ctx, boolean keepAlive)
        throws Exception {
        byte[] body;
        if (response.isSuccess()) {
            if (response.getResult() == null) {
                body = new byte[] {};
            } else {
                Encoder encoder = pickEncoder(response.getResult().getClass());
                if (encoder == null) {
                    writeErrorResponse(INTERNAL_SERVER_ERROR.code(), SERVER_ERROR_MESSAGE, ctx);
                    CommandCenterLog.warn("Error when encoding object",
                        new IllegalStateException("No compatible encoder"));
                    return;
                }
                body = encoder.encode(response.getResult());
            }
        } else {
            body = response.getException().getMessage().getBytes(SentinelConfig.charset());
        }

        HttpResponseStatus status = response.isSuccess() ? OK : BAD_REQUEST;

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
            Unpooled.copiedBuffer(body));

        httpResponse.headers().set("Content-Type", "text/plain; charset=" + SentinelConfig.charset());

        //if (keepAlive) {
        //    httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        //    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        //}
        //ctx.write(httpResponse);
        //if (!keepAlive) {
        //    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        //}
        httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        ctx.write(httpResponse);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    private CommandRequest parseRequest(FullHttpRequest request) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        CommandRequest serverRequest = new CommandRequest();
        Map<String, List<String>> paramMap = queryStringDecoder.parameters();
        // Parse request parameters.
        if (!paramMap.isEmpty()) {
            for (Entry<String, List<String>> p : paramMap.entrySet()) {
                if (!p.getValue().isEmpty()) {
                    serverRequest.addParam(p.getKey(), p.getValue().get(0));
                }
            }
        }
        // Deal with post method, parameter in post has more privilege compared to that in querystring
        if (request.method().equals(HttpMethod.POST)) {
            // support multi-part and form-urlencoded
            HttpPostRequestDecoder postRequestDecoder = null;
            try {
                postRequestDecoder = new HttpPostRequestDecoder(request);
                for (InterfaceHttpData data : postRequestDecoder.getBodyHttpDatas()) {
                    data.retain(); // must retain each attr before destroy
                    if (data.getHttpDataType() == HttpDataType.Attribute) {
                        if (data instanceof HttpData) {
                            HttpData httpData = (HttpData) data;
                            try {
                                String name = httpData.getName();
                                String value = httpData.getString();
                                serverRequest.addParam(name, value);
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            } finally {
                if (postRequestDecoder != null) {
                    postRequestDecoder.destroy();
                }
            }
        }
        // Parse command name.
        String target = parseTarget(queryStringDecoder.rawPath());
        serverRequest.addMetadata(HttpCommandUtils.REQUEST_TARGET, target);
        // Parse body.
        if (request.content().readableBytes() <= 0) {
            serverRequest.setBody(null);
        } else {
            byte[] body = new byte[request.content().readableBytes()];
            request.content().getBytes(0, body);
            serverRequest.setBody(body);
        }
        return serverRequest;
    }

    private String parseTarget(String uri) {
        if (StringUtil.isEmpty(uri)) {
            return "";
        }

        // Remove the / of the uri as the target(command name)
        // Usually the uri is start with /
        int start = uri.indexOf('/');
        if (start != -1) {
            return uri.substring(start + 1);
        }

        return uri;
    }

    private CommandHandler getHandler(String commandName) {
        if (StringUtil.isEmpty(commandName)) {
            return null;
        }
        return HttpServer.handlerMap.get(commandName);
    }

    private void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.write(response);
    }

    private static final String SERVER_ERROR_MESSAGE = "Command server error";
}
