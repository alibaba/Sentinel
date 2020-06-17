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
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.adapter.dubbo.origin.DubboOriginParserRegistry;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

import static com.alibaba.dubbo.common.Constants.PROVIDER;

/**
 * <p>Dubbo service provider filter for Sentinel. Auto activated by default.</p>
 *
 * If you want to disable the provider filter, you can configure:
 * <pre>
 * &lt;dubbo:provider filter="-sentinel.dubbo.provider.filter"/&gt;
 * </pre>
 *
 * @author leyou
 * @author Eric Zhao
 */
@Activate(group = PROVIDER)
public class SentinelDubboProviderFilter extends AbstractDubboFilter implements Filter {

    public SentinelDubboProviderFilter() {
        RecordLog.info("Sentinel Dubbo provider filter initialized");
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Get origin caller.
        String origin = DubboOriginParserRegistry.getDubboOriginParser().parse(invoker, invocation);
        if (null == origin) {
            origin = "";
        }

        Entry interfaceEntry = null;
        Entry methodEntry = null;
        try {
            String resourceName = getResourceName(invoker, invocation, DubboConfig.getDubboProviderPrefix());
            String interfaceName = invoker.getInterface().getName();
            ContextUtil.enter(resourceName, origin);
            interfaceEntry = SphU.entry(interfaceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN);
            methodEntry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_RPC,
                EntryType.IN, invocation.getArguments());

            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                Throwable e = result.getException();
                // Record common exception.
                Tracer.traceEntry(e, interfaceEntry);
                Tracer.traceEntry(e, methodEntry);
            }
            return result;
        } catch (BlockException e) {
            return DubboFallbackRegistry.getProviderFallback().handle(invoker, invocation, e);
        } catch (RpcException e) {
            Tracer.traceEntry(e, interfaceEntry);
            Tracer.traceEntry(e, methodEntry);
            throw e;
        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1, invocation.getArguments());
            }
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
            ContextUtil.exit();
        }
    }
}
