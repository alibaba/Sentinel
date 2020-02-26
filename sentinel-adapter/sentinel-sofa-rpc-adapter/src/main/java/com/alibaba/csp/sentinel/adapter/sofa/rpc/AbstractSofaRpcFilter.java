package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alibaba.csp.sentinel.adapter.sofa.rpc.config.SofaRpcConfig;
import com.alipay.sofa.rpc.common.RpcConfigs;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.AbstractInterfaceConfig;
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
}
