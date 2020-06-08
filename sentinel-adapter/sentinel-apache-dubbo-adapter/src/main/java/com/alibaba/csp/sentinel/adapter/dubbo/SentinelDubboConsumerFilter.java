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
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.InvokeMode;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.util.LinkedList;
import java.util.function.BiConsumer;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * <p>Dubbo service consumer filter for Sentinel. Auto activated by default.</p>
 * <p>
 * If you want to disable the consumer filter, you can configure:
 * <pre>
 * &lt;dubbo:consumer filter="-sentinel.dubbo.consumer.filter"/&gt;
 * </pre>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@Activate(group = CONSUMER)
public class SentinelDubboConsumerFilter extends BaseSentinelDubboFilter {

    public SentinelDubboConsumerFilter() {
        RecordLog.info("Sentinel Apache Dubbo consumer filter initialized");
    }

    @Override
    String getMethodName(Invoker invoker, Invocation invocation) {
        return DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboConsumerPrefix());
    }

    @Override
    String getInterfaceName(Invoker invoker) {
        return DubboUtils.getInterfaceName(invoker);
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        InvokeMode invokeMode = RpcUtils.getInvokeMode(invoker.getUrl(), invocation);
        if (InvokeMode.SYNC == invokeMode) {
            return syncInvoke(invoker, invocation);
        } else {
            return asyncInvoke(invoker, invocation);
        }

    }

    private Result syncInvoke(Invoker<?> invoker, Invocation invocation) {
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        String methodResourceName = getMethodName(invoker, invocation);
        String interfaceResourceName = getInterfaceName(invoker);
        try {
            interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT);
            methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT, invocation.getArguments());
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                Tracer.traceEntry(result.getException(), interfaceEntry);
                Tracer.traceEntry(result.getException(), methodEntry);
            }
            return result;
        } catch (BlockException e) {
            return DubboFallbackRegistry.getConsumerFallback().handle(invoker, invocation, e);
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
        }
    }


    private Result asyncInvoke(Invoker<?> invoker, Invocation invocation) {
        LinkedList<EntryHolder> queue = new LinkedList<>();
        String methodResourceName = getMethodName(invoker, invocation);
        String interfaceResourceName = getInterfaceName(invoker);
        try {
            queue.push(new EntryHolder(SphU.asyncEntry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT), null));
            queue.push(new EntryHolder(SphU.asyncEntry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT, 1, invocation.getArguments()), invocation.getArguments()));
            Result result = invoker.invoke(invocation);
            result.whenCompleteWithContext(new BiConsumer<Result, Throwable>() {
                @Override
                public void accept(Result result, Throwable throwable) {
                    while (!queue.isEmpty()) {
                        EntryHolder holder = queue.pop();
                        Tracer.traceEntry(result.getException(), holder.entry);
                        exitEntry(holder);
                    }
                }
            });
            return result;
        } catch (BlockException e) {
            while (!queue.isEmpty()) {
                exitEntry(queue.pop());
            }
            return DubboFallbackRegistry.getConsumerFallback().handle(invoker, invocation, e);
        }
    }

    class EntryHolder {

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
