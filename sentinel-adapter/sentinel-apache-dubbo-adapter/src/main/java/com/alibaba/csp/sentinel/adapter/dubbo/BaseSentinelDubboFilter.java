/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.dubbo;


import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.ListenableFilter;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;

/**
 * Base Class of the {@link SentinelDubboProviderFilter} and {@link SentinelDubboConsumerFilter}.
 *
 * @author Zechao Zheng
 */

public abstract class BaseSentinelDubboFilter extends ListenableFilter {
    public BaseSentinelDubboFilter() {
        this.listener = new SentinelDubboListener();
    }


    private void traceAndExit(Throwable throwable, Invoker invoker, Invocation invocation) {
        String methodResourceName = getMethodName(invoker, invocation);
        Entry[] entries = (Entry[]) RpcContext.getContext().get(methodResourceName);
        if (entries != null) {
            Entry interfaceEntry = entries[0];
            Entry methodEntry = entries[1];
            if (methodEntry != null) {
                Tracer.traceEntry(throwable, methodEntry);
                methodEntry.exit();
            }
            if (interfaceEntry != null) {
                Tracer.traceEntry(throwable, interfaceEntry);
                interfaceEntry.exit();
            }
            RpcContext.getContext().remove(methodResourceName);
        }
        if (CommonConstants.PROVIDER_SIDE.equals(invoker.getUrl().getParameter(CommonConstants.SIDE_KEY))) {
            ContextUtil.exit();
        }
    }

    /**
     * Get method name of dubbo rpc
     *
     * @param invoker
     * @param invocation
     * @return
     */
    abstract String getMethodName(Invoker invoker, Invocation invocation);

    /**
     * Get interface name of dubbo rpc
     *
     * @param invoker
     * @return
     */
    abstract String getInterfaceName(Invoker invoker);


    private class SentinelDubboListener implements Listener {

        public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
            onSuccess(appResponse, invoker, invocation);
        }

        //for compatible dubbo 2.7.5 rename onResponse to onMessage
        public void onMessage(Result appResponse, Invoker<?> invoker, Invocation invocation) {
            onSuccess(appResponse, invoker, invocation);
        }

        private void onSuccess(Result appResponse, Invoker<?> invoker, Invocation invocation) {
            if (DubboConfig.getDubboBizExceptionTraceEnabled()) {
                traceAndExit(appResponse.getException(), invoker, invocation);
            } else {
                traceAndExit(null, invoker, invocation);
            }
        }

        @Override
        public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
            traceAndExit(t, invoker, invocation);
        }

    }
}
