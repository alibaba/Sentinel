package com.alibaba.csp.sentinel.adapter.dubbo.origin;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * Customized handler parser in Dubbo provider filter. {@link Context#getOrigin()}
 *
 * @author tc
 * @date 2020/6/10
 */
public interface DubboOrigin {

    /**
     * Handle the handler parser.
     *
     * @param invoker    Dubbo invoker
     * @param invocation Dubbo invocation
     * @return handler result
     */
    String handler(Invoker<?> invoker, Invocation invocation);

}
