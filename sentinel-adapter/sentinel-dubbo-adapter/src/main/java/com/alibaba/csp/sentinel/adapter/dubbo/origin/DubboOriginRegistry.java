package com.alibaba.csp.sentinel.adapter.dubbo.origin;

/**
 * Global handler registry for Dubbo.
 *
 * @author tc
 * @date 2020/6/10
 */
public final class DubboOriginRegistry {

    private static volatile DubboOrigin dubboOrigin = new DefaultDubboOrigin();

    public static DubboOrigin getDubboOrigin() {
        return dubboOrigin;
    }

    public static void setDubboOrigin(DubboOrigin dubboOrigin) {
        DubboOriginRegistry.dubboOrigin = dubboOrigin;
    }

    private DubboOriginRegistry() {}

}
