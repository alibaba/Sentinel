package com.alibaba.csp.sentinel.slots.block.adaptive;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemStatusListener;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Liu Yiming
 * @date 2019-07-16 16:28
 */
public class AdaptiveRuleChecker {

    protected AtomicLong storedTokens = new AtomicLong(0);
    protected AtomicLong lastFilledTime = new AtomicLong(0);

    private static SystemStatusListener statusListener = null;

    public void checkAdaptive(ResourceWrapper resource, Context context, DefaultNode node, int count)
        throws BlockException {

        Set<AdaptiveRule> rules = AdaptiveRuleManager.getRules(resource);
        if (rules == null || resource == null) {
            return;
        }

        for (AdaptiveRule rule : rules) {
            if (!canPassCheck(rule, context, node, count)) {
                throw new AdaptiveException(rule.getLimitApp(), rule);
            }
        }
    }

    public boolean canPassCheck(AdaptiveRule rule, Context context, DefaultNode node, int acquireCount) {
        long count = (long)(rule.getCount());
        long passQps = (long)(node.getClusterNode().passQps());
        long previousQps = (long)(node.getClusterNode().previousBlockQps());
        int maxToken = rule.getMaxToken();
        syncToken(previousQps, count, maxToken);

        long restToken = storedTokens.get();
        if (passQps + acquireCount <= restToken || passQps + acquireCount <= count) {
            return true;
        }
        return false;
    }

    protected void syncToken(long passQps, long count, int maxToken) {
        long currentTime = TimeUtil.currentTimeMillis();
        currentTime = currentTime - currentTime % 1000;
        long oldLastFillTime = lastFilledTime.get();
        if (currentTime <= oldLastFillTime) {
            return;
        }

        long oldValue = storedTokens.get();
        long newValue = addTokens(currentTime, passQps, count, maxToken);

        if (storedTokens.compareAndSet(oldValue, newValue)) {
            long currentValue = storedTokens.addAndGet(0 - passQps);
            if (currentValue < 0) {
                storedTokens.set(0L);
            }
            lastFilledTime.set(currentTime);
        }

    }

    private long addTokens(long currentTime, long passQps, long count, int maxToken) {
        long oldValue = storedTokens.get();
        long newValue = oldValue;

        if (oldValue < maxToken) {
            if (lastFilledTime.get() == 0) {
                newValue = count;
            } else {
                newValue = (long)(oldValue + (currentTime - lastFilledTime.get()) * count / 1000);
            }

        }

        return Math.min(newValue, maxToken);
    }

    public static double getCurrentCpuUsage() {
        return statusListener.getCpuUsage();
    }
}
