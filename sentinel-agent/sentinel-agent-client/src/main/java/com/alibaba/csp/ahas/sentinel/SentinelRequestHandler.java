package com.alibaba.csp.ahas.sentinel;

import com.alibaba.csp.sentinel.log.RecordLog;

import com.taobao.csp.ahas.transport.api.AgwRequestHandler;
import com.taobao.csp.ahas.transport.api.Request;
import com.taobao.csp.ahas.transport.api.Response;
import com.taobao.csp.ahas.transport.api.Response.Code;

/**
 * @param <R>
 * @author changjun.xcj
 */
public class SentinelRequestHandler<R> extends AgwRequestHandler<R> {

    private com.alibaba.csp.sentinel.command.CommandHandler commandHandler;

    public SentinelRequestHandler(com.alibaba.csp.sentinel.command.CommandHandler<R> commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public Response<R> handle(Request request) {
        if (commandHandler == null) {
            return Response.ofFailure(Code.NOT_FOUND, "Cannot find command handler");
        }
        return convertResponse(commandHandler.handle(convertRequest(request)));
    }

    /**
     * Convert AHAS request to Sentinel request
     *
     * @param request
     * @return
     */
    private com.alibaba.csp.sentinel.command.CommandRequest convertRequest(Request request) {

        com.alibaba.csp.sentinel.command.CommandRequest commandRequest
            = new com.alibaba.csp.sentinel.command.CommandRequest();
        if (request == null) {
            return commandRequest;
        }
        commandRequest.getMetadata().putAll(request.getHeaders());
        commandRequest.getParameters().putAll(request.getParams());
        return commandRequest;
    }

    /**
     * Convert Sentinel response to AHAS response
     *
     * @param response
     * @return
     */
    private Response<R> convertResponse(com.alibaba.csp.sentinel.command.CommandResponse<R> response) {
        if (response == null) {
            return Response.ofFailure(Code.TIMEOUT, "response is null");
        }
        if (response.isSuccess()) {
            return Response.ofSuccess(response.getResult());
        }
        return Response.ofFailure(Code.SERVER_ERROR, response.getException().getMessage());
    }

    @Override
    public void logHandleException(Request request, Exception exception) {
        RecordLog.info("logHandleException, " + request, exception);
    }
}
