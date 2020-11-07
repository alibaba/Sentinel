package com.alibaba.csp.sentinel.adapter.motan;

import com.alibaba.csp.sentinel.adapter.motan.config.MotanAdapterGlobalConfig;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;

/**
 * program: sentinel-parent
 * description: ${description}
 * author: zhangxn8
 **/
@SpiMeta(name = MotanAdapterGlobalConfig.BASE_SENTINEL_MOTAN_FILTER)
@Activation(sequence = 1)
public abstract class BaseMotanSentinelFilter implements Filter {

    /**
     * Get method name of motan rpc
     *
     * @param caller
     * @param request
     * @return
     */
    abstract String getMethodName(Caller<?> caller, Request request, String prefix);

    /**
     * Get interface name of motan rpc
     *
     * @param caller
     * @return
     */
    abstract String getInterfaceName(Caller<?> caller, String prefix);

}
