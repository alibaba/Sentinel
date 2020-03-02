package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.sofa.rpc.config.SofaRpcConfig;
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
        AbstractInterfaceConfig config = invoker.getConfig();

        String enabled = config.getParameter(SofaRpcConfig.SOFA_RPC_SENTINEL_ENABLED);
        if (StringUtils.isNotBlank(enabled)) {
            return Boolean.valueOf(enabled);
        }

        return RpcConfigs.getOrDefaultValue(SofaRpcConfig.SOFA_RPC_SENTINEL_ENABLED, true);
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
