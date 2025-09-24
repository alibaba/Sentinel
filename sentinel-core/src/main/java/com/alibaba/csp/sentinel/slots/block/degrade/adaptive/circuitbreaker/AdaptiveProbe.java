package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util.AdaptiveUtils;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptive probing controller - used for recovery probing when the fuse is in a half-open state
 * Recovery strategy: If the current error rate ≤ the mean of historical error rates + 2 times the standard deviation → recovery
 *
 * @author ylnxwlp
 */
public class AdaptiveProbe {

    private final AdaptiveCircuitBreaker adaptiveCircuitBreaker;
    private final Map<Context, Context> primaryContextMap = new ConcurrentHashMap<>();
    private final Map<Context, Context> successContextMap = new ConcurrentHashMap<>();
    private final Map<Context, Context> failureContextMap = new ConcurrentHashMap<>();
    private final AtomicInteger releaseRequestNumber = new AtomicInteger();
    private final AtomicBoolean isTimeout = new AtomicBoolean(false);
    private volatile int releaseRequestLimit;
    private volatile long halfOpenTimeoutMs;

    public AdaptiveProbe(AdaptiveCircuitBreaker adaptiveCircuitBreaker) {
        this.adaptiveCircuitBreaker = adaptiveCircuitBreaker;
    }

    public void setReleaseRequestLimit(int releaseRequestLimit) {
        this.releaseRequestLimit = releaseRequestLimit;
    }

    public void setHalfOpenTimeoutMs(long halfOpenTimeoutMs) {
        this.halfOpenTimeoutMs = halfOpenTimeoutMs;
    }

    /**
     * Receive the probing request.
     */
    public boolean handleProbeRequest(Context context) {
        if (TimeUtil.currentTimeMillis() >= halfOpenTimeoutMs) {
            return false;
        }
        int current;
        do {
            current = releaseRequestNumber.get();
            if (current >= releaseRequestLimit) {
                return false;
            }
        } while (!releaseRequestNumber.compareAndSet(current, current + 1));

        primaryContextMap.putIfAbsent(context, context);
        return true;
    }

    /**
     * When the test request is completed, check the test situation.
     */
    public ProbeResults handleProbeRequestOnComplete(Context context) {
        if (TimeUtil.currentTimeMillis() >= halfOpenTimeoutMs) {
            resetState();
            return ProbeResults.FAIL;
        }

        Context storedContext = primaryContextMap.remove(context);
        if (storedContext != null) {
            (context.getCurEntry().getError() == null ? successContextMap : failureContextMap)
                    .put(storedContext, storedContext);
        }

        int totalCollected = failureContextMap.size() + successContextMap.size();
        if (primaryContextMap.isEmpty() && totalCollected == releaseRequestLimit && isTimeout.compareAndSet(false, true)) {
            boolean ok = AdaptiveUtils.isProbeSuccess(
                    successContextMap.size(),
                    failureContextMap.size(),
                    adaptiveCircuitBreaker.getWindows()
            );
            ProbeResults result = ok ? ProbeResults.SUCCESS : ProbeResults.FAIL;
            resetState();
            RecordLog.warn("[AdaptiveProbe] time:{},resource:{},probe result:{}", TimeUtil.currentTimeMillis(), adaptiveCircuitBreaker.getResourceName(), result);
            return result;
        }
        return ProbeResults.WAITING;
    }

    private void resetState() {
        primaryContextMap.clear();
        successContextMap.clear();
        failureContextMap.clear();
        releaseRequestNumber.set(0);
        isTimeout.set(false);
    }

    public enum ProbeResults {
        FAIL, WAITING, SUCCESS
    }
}