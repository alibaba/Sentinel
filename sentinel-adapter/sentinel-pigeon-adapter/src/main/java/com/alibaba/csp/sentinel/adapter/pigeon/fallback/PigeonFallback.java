package com.alibaba.csp.sentinel.adapter.pigeon.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;

@FunctionalInterface
public interface PigeonFallback {

    /**
     * Handle the block exception and provide fallback result.
     *
     * @return fallback result
     */
    <T> void handle(T context, BlockException ex);
}
