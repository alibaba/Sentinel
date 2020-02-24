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
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.*;

/**
 * Base Class of the {@link SentinelDubboProviderFilter} and {@link SentinelDubboConsumerFilter}.
 *
 * @author Zechao Zheng
 */

public abstract class BaseSentinelDubboFilter extends ListenableFilter {
    public BaseSentinelDubboFilter() {
        this.listener = new SentinelDubboListener();
    }

    static class SentinelDubboListener implements Listener {

        public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
            onSuccess(appResponse, invoker);
        }

        //for compatible dubbo 2.7.5 rename onResponse to onMessage
        public void onMessage(Result appResponse, Invoker<?> invoker, Invocation invocation) {
            onSuccess(appResponse, invoker);
        }

        private void onSuccess(Result appResponse, Invoker<?> invoker) {
            if (DubboConfig.getDubboBizExceptionTraceEnabled()) {
                traceAndExit(appResponse.getException(), invoker.getUrl());
            } else {
                traceAndExit(null, invoker.getUrl());
            }
        }

        @Override
        public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
            traceAndExit(t, invoker.getUrl());
        }

    }

    static void traceAndExit(Throwable throwable, URL url) {
        Entry interfaceEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY);
        Entry methodEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_METHOD_ENTRY_KEY);
        if (methodEntry != null) {
            Tracer.traceEntry(throwable, methodEntry);
            methodEntry.exit();
            RpcContext.getContext().remove(DubboUtils.DUBBO_METHOD_ENTRY_KEY);
        }
        if (interfaceEntry != null) {
            Tracer.traceEntry(throwable, interfaceEntry);
            interfaceEntry.exit();
            RpcContext.getContext().remove(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY);
        }
        if (CommonConstants.PROVIDER_SIDE.equals(url.getParameter(CommonConstants.SIDE_KEY))) {
            ContextUtil.exit();
        }
    }
}
