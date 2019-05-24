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
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderInterceptor;

import java.lang.reflect.Parameter;

public class SentinelPigeonProviderInterceptor implements ProviderInterceptor {

    @Override
    public void preInvoke(ProviderContext providerContext) {
        try {
            String resourceName = PigeonUtils.getResourceName(providerContext);
            Parameter[] arguments = PigeonUtils.getMethodArguments(providerContext);
            ContextUtil.enter(resourceName);
            SphU.entry(resourceName, EntryType.IN, 1, arguments);
        } catch (BlockException ex) {
            PigeonFallbackRegistry.getProviderFallback().handle(providerContext, ex);
        }
    }

    @Override
    public void postInvoke(ProviderContext providerContext) {
        Entry methodEntry = ContextUtil.getContext().getCurEntry();
        Parameter[] arguments = PigeonUtils.getMethodArguments(providerContext);

        Throwable ex = null;
        ex = providerContext.getServiceError();
        if (ex != null) {
            Tracer.traceEntry(ex, methodEntry);
        }
        //because of service error and framework error are mutually exclusive
        ex = providerContext.getFrameworkError();
        if (ex != null) {
            Tracer.traceEntry(ex, methodEntry);
        }

        if (methodEntry != null) {
            methodEntry.exit(1, arguments);
        }
        ContextUtil.exit();
    }

}
