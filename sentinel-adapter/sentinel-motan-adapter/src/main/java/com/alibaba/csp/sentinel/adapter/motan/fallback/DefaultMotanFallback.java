package com.alibaba.csp.sentinel.adapter.motan.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.DefaultResponse;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;

/**
 * program: sentinel-parent
 * description: ${description}
 * author: zxn
 * create: 2020-10-28 00:36
 **/
public class DefaultMotanFallback implements MotanFallback{

    @Override
    public Response handle(Caller<?> caller, Request request, BlockException ex) {
        DefaultResponse defaultResponse = new DefaultResponse();
        defaultResponse.setException(ex.toRuntimeException());
        defaultResponse.setRequestId(request.getRequestId());
        defaultResponse.setAttachments(request.getAttachments());
        defaultResponse.setRpcProtocolVersion(request.getRpcProtocolVersion());
        return defaultResponse;
    }
}
