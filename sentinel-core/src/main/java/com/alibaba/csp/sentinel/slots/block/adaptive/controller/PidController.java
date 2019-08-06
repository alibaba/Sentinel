package com.alibaba.csp.sentinel.slots.block.adaptive.controller;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.concurrent.atomic.AtomicLong;

public class PidController implements TrafficShapingController {
    private double expectRt;
    private double targetRatio; // 目标通过率

    protected AtomicLong err = new AtomicLong();
    protected AtomicLong err_last = new AtomicLong();
    protected AtomicLong integral = new AtomicLong(); // 积分值

    // 令牌发放速率
    protected AtomicLong bucketCount = new AtomicLong(2000);

    // 剩余存储令牌数
    protected AtomicLong storedTokens = new AtomicLong(0);

    // 上一次存令牌的时间
    protected AtomicLong lastFilledTime = new AtomicLong(0);

    // 上一次更新令牌发放速率的时间
    protected AtomicLong lastCountTime = new AtomicLong(0);

    // 令牌桶容量
    protected AtomicLong maxTokens = new AtomicLong(4000);

    public PidController(double targetRatio, double expectRt) {
        this.targetRatio = targetRatio;
        this.expectRt = expectRt;
    }

    @Override
    public boolean canPass(Node node, int acquireCount) {
        return canPass(node, acquireCount, false);
    }

    @Override
    public boolean canPass(Node node, int acquireCount, boolean prioritized) {
        long previousQps = (long)(node.previousPassQps());
        long passQps = (long)(node.passQps());

        syncBucketCount((ClusterNode) node, targetRatio, expectRt);

        long newCount = bucketCount.get();
        syncToken(previousQps, newCount, maxTokens.get());

        long restToken = storedTokens.get();

        if (passQps + acquireCount <= restToken + newCount) {
            return true;
        }
        return false;
    }

    protected synchronized void syncBucketCount(ClusterNode node, double targetRatio, double expectRt) {
        long currentTime = TimeUtil.currentTimeMillis();
        currentTime = currentTime - currentTime % 1000;
        long oldCountTime = lastCountTime.get();
        if (currentTime <= oldCountTime) { return; }
        if (oldCountTime == 0) {
            lastCountTime.compareAndSet(oldCountTime, lastFilledTime.get());
            return;
        }
        if ((currentTime - oldCountTime) / 1000 > 0.9) {
            double ratio;
            long newCount = bucketCount.get();

            double smoothing = 0.2;

            long previousQps = (long)(node.previousPassQps());
            long previousTotalQps = (long)(node.previousBlockQps()) + previousQps;
            long passQps = (long)(node.passQps());
            long totalQps = (long)(node.totalQps());
            if (totalQps != 0) {
                ratio = passQps / totalQps;
                // 如果newCount比用户预设的通过率小很多，就让其快速增加
                if (newCount < totalQps * targetRatio / 2) {
                    bucketCount.compareAndSet(newCount, (long)(totalQps * targetRatio / 2));
                    newCount = bucketCount.get();
                }
                //System.out.println("statistic");
                // 计算限制范围[1.0, 5.0]的梯度
                // 实际通过率越小，梯度值越大，以增加后续通过数
                double gradientRatio = Math.max(1.0, Math.min(2.0, targetRatio / ratio));

                double avgRt = node.avgRt();
                double longTermRt = node.longTermRt();
                double gradientRt;
                if (avgRt == 0) {
                    gradientRt = 1;
                } else {
                    // 计算限制范围[0.5, 1.0]的梯度以过滤异常值
                    // 实际 avgRt 越大，梯度值越小，说明需要限制通过数
                    double needRt = Math.min(longTermRt, expectRt);
                    gradientRt = Math.max(0.5, Math.min(1.0, needRt / avgRt));
                }

                long currentCount = bucketCount.get();

//                // 最后的令牌发放速度由通过率和 Rt 共同决定
//                newCount = (long)(gradientRatio * gradientRt * currentCount);
//                // 使用平滑因子更新令牌发放速度（默认为0.2）
//                newCount = (long)(currentCount * (1 - smoothing) + newCount * smoothing);

                newCount = currentCount + syncCountByPid(node, expectRt);

                bucketCount.compareAndSet(currentCount, newCount);


            }
            long newMaxToken = Math.max(newCount, (long)(previousTotalQps * targetRatio));
            long oldMaxToken = maxTokens.get();
            if (newMaxToken > oldMaxToken) {
                maxTokens.compareAndSet(oldMaxToken, newMaxToken);
            }
            lastCountTime.compareAndSet(oldCountTime, currentTime);
        }
    }

    protected long syncCountByPid(ClusterNode node, double expectRt) {
        double Kp = 1000, Ki =5, Kd = 0;
        double errNum = expectRt - node.avgRt();
        err.compareAndSet(err.get(), Double.doubleToLongBits(errNum));
        double integralNum = Double.longBitsToDouble(integral.get()) + errNum;
        integral.compareAndSet(integral.get(), Double.doubleToLongBits(integralNum));
        double countNum = Kp * errNum + Ki * integralNum + Kd * (errNum - Double.longBitsToDouble(err_last.get()));
        err_last.compareAndSet(err_last.get(), Double.doubleToLongBits(errNum));
        return (long)countNum;
    }

    protected void syncToken(long passQps, long count, long maxToken) {
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

    private long addTokens(long currentTime, long passQps, long count, long maxToken) {
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
}
