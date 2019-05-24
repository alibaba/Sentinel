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
package com.alibaba.csp.sentinel.adapter.pigeon;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.pigeon.fallback.PigeonFallbackRegistry;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.InvokerInterceptor;

public class SentinelPigeonInvokerInterceptor implements InvokerInterceptor {

    @Override
    public void preInvoke(InvokerContext invokerContext) {
        try {
            String resourceName = PigeonUtils.getResourceName(invokerContext);
            SphU.entry(resourceName, EntryType.OUT);
        } catch (BlockException ex) {
            PigeonFallbackRegistry.getInvokerFallback().handle(invokerContext, ex);
        }
    }

    @Override
    public void postInvoke(InvokerContext invokerContext) {
        Entry methodEntry = ContextUtil.getContext().getCurEntry();
        if (methodEntry != null) {
            methodEntry.exit();
        }
    }

    @Override
    public void afterThrowing(InvokerContext invokerContext, Throwable throwable) {
        Entry methodEntry = ContextUtil.getContext().getCurEntry();
        Tracer.traceEntry(throwable, methodEntry);
        if (methodEntry != null) {
            methodEntry.exit();
        }
    }

}
