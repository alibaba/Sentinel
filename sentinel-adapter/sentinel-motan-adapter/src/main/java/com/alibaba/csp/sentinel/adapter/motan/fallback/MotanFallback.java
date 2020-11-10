package com.alibaba.csp.sentinel.adapter.motan.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;

/**
 * @author zhangxn8
 */
public interface MotanFallback {

    /**
     * Handle the block exception and provide fallback result.
     * @param caller
     * @param request
     * @param ex
     * @return
     */
    Response handle(Caller<?> caller, Request request, BlockException ex);

}
