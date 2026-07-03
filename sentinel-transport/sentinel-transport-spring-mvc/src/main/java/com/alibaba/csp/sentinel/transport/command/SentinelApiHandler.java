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
package com.alibaba.csp.sentinel.transport.command;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.command.http.StatusCode;
import com.alibaba.csp.sentinel.transport.log.CommandCenterLog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author shenbaoyong
 */
public class SentinelApiHandler {

    public static final String SERVER_ERROR_MESSAGE = "Command server error";

    private CommandHandler commandHandler;

    public SentinelApiHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        PrintWriter printWriter = null;
        try {
            long start = System.currentTimeMillis();
            printWriter = httpServletResponse.getWriter();
            CommandCenterLog.debug("[SentinelApiHandler] request income: {}", httpServletRequest.getRequestURL());
            CommandRequest request = new CommandRequest();
            Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] value = entry.getValue();
                if (value != null && value.length >= 1) {
                    request.addParam(entry.getKey(), value[0]);
                }
            }
            CommandResponse<?> response = commandHandler.handle(request);
            handleResponse(response, httpServletResponse, printWriter);

            long cost = System.currentTimeMillis() - start;
            CommandCenterLog.debug("[SentinelApiHandler] Deal request: {}, time cost: {} ms", httpServletRequest.getRequestURL(), cost);
        } catch (Throwable e) {
            CommandCenterLog.warn("[SentinelApiHandler] error", e);
            try {
                if (printWriter != null) {
                    writeResponse(httpServletResponse, printWriter, StatusCode.INTERNAL_SERVER_ERROR, SERVER_ERROR_MESSAGE);
                }
            } catch (Exception e1) {
                CommandCenterLog.warn("Failed to write error response", e1);
            }
        }
    }

    private void writeResponse(HttpServletResponse httpServletResponse, PrintWriter out, StatusCode statusCode, String message) {
        httpServletResponse.setStatus(statusCode.getCode());
        if (message != null) {
            out.print(message);
        }
        out.flush();
    }

    private <T> void handleResponse(CommandResponse<T> response, HttpServletResponse httpServletResponse, final PrintWriter printWriter) throws Exception {
        if (response.isSuccess()) {
            if (response.getResult() == null) {
                writeResponse(httpServletResponse, printWriter, StatusCode.OK, null);
                return;
            }
            // Here we directly use `toString` to encode the result to plain text.
            byte[] buffer = response.getResult().toString().getBytes(SentinelConfig.charset());
            writeResponse(httpServletResponse, printWriter, StatusCode.OK, new String(buffer));
        } else {
            String msg = SERVER_ERROR_MESSAGE;
            if (response.getException() != null) {
                msg = response.getException().getMessage();
            }
            writeResponse(httpServletResponse, printWriter, StatusCode.BAD_REQUEST, msg);
        }
    }

}
