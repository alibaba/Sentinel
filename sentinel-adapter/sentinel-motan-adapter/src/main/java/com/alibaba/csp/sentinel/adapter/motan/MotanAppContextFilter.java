package com.alibaba.csp.sentinel.adapter.motan;

import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.rpc.RpcContext;

import java.util.Map;

/**
 * program: sentinel-parent
 * description: ${description}
 * author: zxn
 * create: 2020-10-28 00:28
 **/
@SpiMeta(name = "motanAppContext")
@Activation(sequence = 2)
public class MotanAppContextFilter implements Filter {

    @Override
    public Response filter(Caller<?> caller, Request request) {
        if (request != null){
            Map<String, String> attachment = request.getAttachments();
            RpcContext.getContext().setRpcAttachment("application", attachment.getOrDefault("application", "motan"));
        }
        return caller.call(request);
    }
}
