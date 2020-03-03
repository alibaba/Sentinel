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
package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.sofa.rpc.fallback.SofaRpcFallbackRegistry;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import com.alipay.sofa.rpc.filter.FilterInvoker;

import static com.alibaba.csp.sentinel.adapter.sofa.rpc.SofaRpcUtils.getInterfaceResourceName;
import static com.alibaba.csp.sentinel.adapter.sofa.rpc.SofaRpcUtils.getMethodResourceName;
import static com.alibaba.csp.sentinel.adapter.sofa.rpc.SofaRpcUtils.getMethodArguments;

/**
 * SOFARPC service consumer filter for Sentinel, auto activated by default.
 *
 * If you want to disable the consumer filter, you can configure:
 * <pre>ConsumerConfig.setParameter("sofa.rpc.sentinel.enabled", "false");</pre>
 *
 * or add setting in rpc-config.json:
 * <pre>"sofa.rpc.sentinel.enabled": false </pre>
 *
 * @author cdfive
 */
@Extension(value = "consumerSentinel", order = -1000)
@AutoActive(consumerSide = true)
public class SentinelSofaRpcConsumerFilter extends AbstractSofaRpcFilter {

    @Override
    public SofaResponse invoke(FilterInvoker invoker, SofaRequest request) throws SofaRpcException {
        // Now only support sync invoke.
        if (request.getInvokeType() != null && !RpcConstants.INVOKER_TYPE_SYNC.equals(request.getInvokeType())) {
            return invoker.invoke(request);
        }

        String interfaceResourceName = getInterfaceResourceName(request);
        String methodResourceName = getMethodResourceName(request);

        Entry interfaceEntry = null;
        Entry methodEntry = null;
        try {
            interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT);
            methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC,
                EntryType.OUT, getMethodArguments(request));

            SofaResponse response = invoker.invoke(request);

            traceResponseException(response, interfaceEntry, methodEntry);
            return response;
        } catch (BlockException e) {
            return SofaRpcFallbackRegistry.getConsumerFallback().handle(invoker, request, e);
        } catch (Throwable t) {
            throw traceOtherException(t, interfaceEntry, methodEntry);
        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1, getMethodArguments(request));
            }

            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
        }
    }
}
