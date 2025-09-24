package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Circuit breakers' probe management.
 *
 * @author ylnxwlp
 */
public class AdaptiveProbeManager {
    private static final Map<AdaptiveCircuitBreaker, AdaptiveProbe> probeMap = new ConcurrentHashMap<>();

    public static AdaptiveProbe getProbe(AdaptiveCircuitBreaker adaptiveCircuitBreaker) {
        AdaptiveProbe probe = probeMap.get(adaptiveCircuitBreaker);
        if (probe == null) {
            probe = new AdaptiveProbe(adaptiveCircuitBreaker);
            AdaptiveProbe existingProbe = probeMap.putIfAbsent(adaptiveCircuitBreaker, probe);
            if (existingProbe != null) {
                probe = existingProbe;
            }
        }
        return probe;
    }

    public static AdaptiveProbe removeProbe(AdaptiveCircuitBreaker adaptiveCircuitBreaker) {
        return probeMap.remove(adaptiveCircuitBreaker);
    }

    public void clearAllProbes() {
        probeMap.clear();
    }

    public int getProbeCount() {
        return probeMap.size();
    }
}
