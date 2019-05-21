package com.alibaba.csp.sentinel.adapter.pigeon.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;

public class DefaultPigeonFallback implements PigeonFallback {

    @Override
    public <T> void handle(T context, BlockException ex) {
        // Just wrap and throw the exception.
        throw new SentinelRpcException(ex);
    }
}
