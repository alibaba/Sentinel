package com.alibaba.csp.sentinel.adapter.dubbo.origin;

import com.alibaba.csp.sentinel.adapter.dubbo.DubboUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * Default Dubbo origin parse.
 *
 * @author tc
 * @date 2020/6/10
 */
public class DefaultDubboOriginParser implements DubboOriginParser {

    @Override
    public String parse(Invoker<?> invoker, Invocation invocation) {
        return DubboUtils.getApplication(invocation, "");
    }

}
