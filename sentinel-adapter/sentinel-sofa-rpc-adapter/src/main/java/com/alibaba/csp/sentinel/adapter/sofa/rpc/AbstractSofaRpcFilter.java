/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;

import com.alipay.sofa.rpc.common.RpcConfigs;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.AbstractInterfaceConfig;
import com.alipay.sofa.rpc.core.exception.RpcErrorType;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.Filter;
import com.alipay.sofa.rpc.filter.FilterInvoker;

/**
 * @author cdfive
 */
abstract class AbstractSofaRpcFilter extends Filter {

    @Override
    public boolean needToLoad(FilterInvoker invoker) {
        AbstractInterfaceConfig<?, ?> config = invoker.getConfig();

        String enabled = config.getParameter(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED);
        if (StringUtils.isNotBlank(enabled)) {
            return Boolean.parseBoolean(enabled);
        }

        return RpcConfigs.getOrDefaultValue(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, true);
    }

    protected void traceResponseException(SofaResponse response, Entry interfaceEntry, Entry methodEntry) {
        if (response.isError()) {
            SofaRpcException rpcException = new SofaRpcException(RpcErrorType.SERVER_FILTER, response.getErrorMsg());
            Tracer.traceEntry(rpcException, interfaceEntry);
            Tracer.traceEntry(rpcException, methodEntry);
        } else {
            Object appResponse = response.getAppResponse();
            if (appResponse instanceof Throwable) {
                Tracer.traceEntry((Throwable) appResponse, interfaceEntry);
                Tracer.traceEntry((Throwable) appResponse, methodEntry);
            }
        }
    }

    protected SofaRpcException traceOtherException(Throwable t, Entry interfaceEntry, Entry methodEntry) {
        SofaRpcException rpcException;
        if (t instanceof SofaRpcException) {
            rpcException = (SofaRpcException) t;
        } else {
            rpcException = new SofaRpcException(RpcErrorType.SERVER_FILTER, t);
        }
        Tracer.traceEntry(rpcException, interfaceEntry);
        Tracer.traceEntry(rpcException, methodEntry);
        return rpcException;
    }
}
