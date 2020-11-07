package com.alibaba.csp.sentinel.adapter.motan;

import com.alibaba.csp.sentinel.adapter.motan.config.MotanAdapterGlobalConfig;
import com.weibo.api.motan.common.MotanConstants;
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
 * author: zhangxn8
 **/
@SpiMeta(name = MotanAdapterGlobalConfig.MOTAN_APP_CONTEXT)
@Activation(sequence = 2, key = { MotanConstants.NODE_TYPE_SERVICE, MotanConstants.NODE_TYPE_REFERER })
public class MotanAppContextFilter implements Filter {

    @Override
    public Response filter(Caller<?> caller, Request request) {
        if (request != null) {
            Map<String, String> attachment = request.getAttachments();
            RpcContext.getContext().setRpcAttachment(MotanAdapterGlobalConfig.APPLICATION, attachment.getOrDefault(MotanAdapterGlobalConfig.APPLICATION, MotanAdapterGlobalConfig.MOTAN));
        }
        return caller.call(request);
    }
}
