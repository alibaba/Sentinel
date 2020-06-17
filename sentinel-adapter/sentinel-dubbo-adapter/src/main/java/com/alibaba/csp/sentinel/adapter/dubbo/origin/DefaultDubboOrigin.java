package com.alibaba.csp.sentinel.adapter.dubbo.origin;

import com.alibaba.csp.sentinel.adapter.dubbo.DubboUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * Default Dubbo origin handler.
 *
 * @author tc
 * @date 2020/6/10
 */
public class DefaultDubboOrigin implements DubboOrigin {

    @Override
    public String handler(Invoker<?> invoker, Invocation invocation) {
        return DubboUtils.getApplication(invocation, "");
    }

}
