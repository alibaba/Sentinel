package com.alibaba.csp.sentinel.adapter.dubbo.origin;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * Customized origin parse in Dubbo provider filter. {@link Context#getOrigin()}
 *
 * @author tc
 * @date 2020/6/10
 */
public interface DubboOriginParser {

    /**
     * Handle the origin parse.
     *
     * @param invoker    Dubbo invoker
     * @param invocation Dubbo invocation
     * @return parse result
     */
    String parse(Invoker<?> invoker, Invocation invocation);

}
