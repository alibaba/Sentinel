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
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

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
        String origin = DubboAdapterGlobalConfig.getOriginParser().parse(invoker, invocation);
        if (null == origin) {
            origin = "";
        }
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        String prefix = DubboAdapterGlobalConfig.getDubboProviderResNamePrefixKey();
        String interfaceResourceName = getInterfaceName(invoker, prefix);
        String methodResourceName = getMethodName(invoker, invocation, prefix);
        try {
            // Only need to create entrance context at provider side, as context will take effect
            // at entrance of invocation chain only (for inbound traffic).
            ContextUtil.enter(methodResourceName, origin);
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

