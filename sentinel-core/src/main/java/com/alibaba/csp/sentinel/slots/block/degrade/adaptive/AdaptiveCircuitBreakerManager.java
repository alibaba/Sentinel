package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Management class of adaptive circuit breaker.
 *
 * @author ylnxwlp
 */
public class AdaptiveCircuitBreakerManager {

    private static final ConcurrentHashMap<String, AdaptiveCircuitBreaker> CIRCUIT_BREAKER_MAP =
            new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Lock> LOCK_MAP = new ConcurrentHashMap<>();

    public static AdaptiveCircuitBreaker getAdaptiveCircuitBreaker(String resourceName) {
        AdaptiveCircuitBreaker breaker = CIRCUIT_BREAKER_MAP.get(resourceName);
        if (breaker == null) {
            Lock lock = LOCK_MAP.computeIfAbsent(resourceName, k -> new ReentrantLock());
            try {
                lock.lock();

                breaker = CIRCUIT_BREAKER_MAP.get(resourceName);
                if (breaker == null) {
                    breaker = new AdaptiveCircuitBreaker(resourceName);
                    CIRCUIT_BREAKER_MAP.put(resourceName, breaker);
                }
            } finally {
                lock.unlock();
            }
        }
        return breaker;
    }

    public static void removeAdaptiveCircuitBreaker(String resourceName) {
        CIRCUIT_BREAKER_MAP.remove(resourceName);
        LOCK_MAP.remove(resourceName);
    }

    public static void clearAll() {
        CIRCUIT_BREAKER_MAP.clear();
        LOCK_MAP.clear();
    }
}
