package com.alibaba.csp.sentinel.adapter.pigeon.fallback;

import com.alibaba.csp.sentinel.util.AssertUtil;

public final class PigeonFallbackRegistry {

    private static volatile PigeonFallback invokerFallback = new DefaultPigeonFallback();
    private static volatile PigeonFallback providerFallback = new DefaultPigeonFallback();

    public static PigeonFallback getConsumerFallback() {
        return invokerFallback;
    }

    public static void setInvokerFallback(PigeonFallback invokerFallback) {
        AssertUtil.notNull(invokerFallback, "invokerFallback cannot be null");
        PigeonFallbackRegistry.invokerFallback = invokerFallback;
    }

    public static PigeonFallback getProviderFallback() {
        return providerFallback;
    }

    public static void setProviderFallback(PigeonFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        PigeonFallbackRegistry.providerFallback = providerFallback;
    }

    private PigeonFallbackRegistry() {}

}
