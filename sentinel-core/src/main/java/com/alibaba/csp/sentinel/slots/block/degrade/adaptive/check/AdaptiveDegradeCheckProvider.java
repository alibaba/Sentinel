package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.ServiceLoader;

/**
 * SPI service provider, used to obtain the implementation instance of AdaptiveDegradeCheck.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeCheckProvider {

    private static volatile AdaptiveDegradeCheck instance;

    public static AdaptiveDegradeCheck getInstance() {
        if (instance == null) {
            synchronized (AdaptiveDegradeCheckProvider.class) {
                if (instance == null) {
                    ServiceLoader<AdaptiveDegradeCheck> serviceLoader = ServiceLoader.load(AdaptiveDegradeCheck.class);

                    for (AdaptiveDegradeCheck check : serviceLoader) {
                        instance = check;
                        break;
                    }

                    if (instance == null) {
                        RecordLog.debug("[AdaptiveDegradeCheckProvider] There is no user-defined adaptive circuit breaker algorithm");
                        instance = new DefaultAdaptiveDegradeCheck();
                    }
                }
            }
        }
        return instance;
    }
}
