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

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.util.LinkedList;
import java.util.Optional;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * <p>Apache Dubbo service provider filter that enables integration with Sentinel. Auto activated by default.</p>
 * <p>Note: this only works for Apache Dubbo 2.7.x or above version.</p>
 * <p>
 * If you want to disable the provider filter, you can configure:
 * <pre>
 * &lt;dubbo:provider filter="-sentinel.dubbo.provider.filter"/&gt;
 * </pre>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@Activate(group = PROVIDER)
public class SentinelDubboProviderFilter extends BaseSentinelDubboFilter {

    public SentinelDubboProviderFilter() {
        RecordLog.info("Sentinel Apache Dubbo provider filter initialized");
    }

    @Override
    String getMethodName(Invoker invoker, Invocation invocation, String prefix) {
        return DubboUtils.getMethodResourceName(invoker, invocation, prefix);
    }

    @Override
    String getInterfaceName(Invoker invoker, String prefix) {
        return DubboUtils.getInterfaceName(invoker, prefix);
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Get origin caller.
        InvokeMode invokeMode = RpcUtils.getInvokeMode(invoker.getUrl(), invocation);
        String origin = DubboAdapterGlobalConfig.getOriginParser().parse(invoker, invocation);
        if (null == origin) {
            origin = "";
        }
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        String prefix = DubboAdapterGlobalConfig.getDubboProviderResNamePrefixKey();
        String interfaceResourceName = getInterfaceName(invoker, prefix);
        String methodResourceName = getMethodName(invoker, invocation, prefix);
        ContextUtil.enter(methodResourceName, origin);
        try {
            // Only need to create entrance context at provider side, as context will take effect
            // at entrance of invocation chain only (for inbound traffic).
            if (InvokeMode.ASYNC == invokeMode) {
                return asyncInvoke(invoker, invocation, interfaceResourceName, methodResourceName);
            } else {
                return syncInvoke(invoker, invocation, interfaceResourceName, methodResourceName);
            }
        } catch (RpcException e) {
            Tracer.traceEntry(e, interfaceEntry);
            Tracer.traceEntry(e, methodEntry);
            throw e;
        } finally {
            ContextUtil.exit();
        }
    }


    private Result syncInvoke(Invoker<?> invoker, Invocation invocation, String interfaceResourceName, String methodResourceName) {
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        try {
            interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN);
            methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN,
                    invocation.getArguments());
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                Tracer.traceEntry(result.getException(), interfaceEntry);
                Tracer.traceEntry(result.getException(), methodEntry);
            }
            return result;
        } catch (BlockException e) {
            return DubboAdapterGlobalConfig.getProviderFallback().handle(invoker, invocation, e);
        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1, invocation.getArguments());
            }
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
        }
    }


    private Result asyncInvoke(Invoker<?> invoker, Invocation invocation, String interfaceResourceName, String methodResourceName) {
        LinkedList<EntryHolder> queue = new LinkedList<>();
        try {
            queue.push(new EntryHolder(
                    SphU.asyncEntry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN), null));
            queue.push(new EntryHolder(
                    SphU.asyncEntry(methodResourceName, ResourceTypeConstants.COMMON_RPC,
                            EntryType.IN, invocation.getArguments()), invocation.getArguments()));
            Result result = invoker.invoke(invocation);
            result.whenCompleteWithContext((r, throwable) -> {
                Throwable error = throwable;
                if (error == null) {
                    error = Optional.ofNullable(r).map(Result::getException).orElse(null);
                }
                while (!queue.isEmpty()) {
                    EntryHolder holder = queue.pop();
                    Tracer.traceEntry(error, holder.entry);
                    exitEntry(holder);
                }
            });
            return result;
        } catch (BlockException e) {
            while (!queue.isEmpty()) {
                exitEntry(queue.pop());
            }
            return DubboAdapterGlobalConfig.getProviderFallback().handle(invoker, invocation, e);
        }
    }

    static class EntryHolder {

        final private Entry entry;
        final private Object[] params;

        public EntryHolder(Entry entry, Object[] params) {
            this.entry = entry;
            this.params = params;
        }
    }

    private void exitEntry(EntryHolder holder) {
        if (holder.params != null) {
            holder.entry.exit(1, holder.params);
        } else {
            holder.entry.exit();
        }
    }

}

