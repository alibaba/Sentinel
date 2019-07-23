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
    protected AtomicLong bucketCount = new AtomicLong(20000);
    //protected long bucketCount = 2000;
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
        double targetRadio = rule.getTargetRadio(); //目标通过率
        int maxToken = rule.getMaxToken();
        double smoothing = 0.2;

        long previousQps = (long)(node.getClusterNode().previousBlockQps());
        long passQps = (long)(node.getClusterNode().passQps());
        long totalQps = (long)(node.getClusterNode().totalQps());

        double radio;
        long newCount = bucketCount.get();
        if (totalQps != 0) {
            radio = passQps / totalQps;
            if (Math.random() < (1 / totalQps)) {

                // 计算限制范围[1.0, 2.0]的梯度
                // 实际通过率越小，梯度值越大，以增加后续通过数
                double gradientRadio = Math.max(1.0, Math.min(2.0, targetRadio / radio));

                long avgRt = (long)(node.getClusterNode().avgRt());
                long longTermRt = (long)(node.getClusterNode().longTermRt());
                double gradientRt;
                if (avgRt == 0) {
                    gradientRt = 1;
                } else {
                    // 计算限制范围[0.5, 1.0]的梯度以过滤异常值
                    // 实际 avgRt 越大，梯度值越小，说明需要限制通过数
                    gradientRt = Math.max(0.5, Math.min(1.0, longTermRt / avgRt));
                }

                long currentCount = bucketCount.get();

                // 最后的令牌发放速度由通过率和 Rt 共同决定
                newCount = (long)(gradientRadio * gradientRt * currentCount);
                // 使用平滑因子更新令牌发放速度（默认为0.2）
                newCount = (long)(currentCount * (1 - smoothing) + newCount * smoothing);
                bucketCount.compareAndSet(currentCount, newCount);
            }
        }

        syncToken(previousQps, newCount, maxToken);

        long restToken = storedTokens.get();
        //newCount = Math.max(newCount, restToken);
        if (passQps + acquireCount <= restToken || passQps + acquireCount <= newCount) {
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
