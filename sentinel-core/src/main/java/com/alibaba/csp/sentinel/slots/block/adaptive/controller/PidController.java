package com.alibaba.csp.sentinel.slots.block.adaptive.controller;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.slots.system.SystemStatusListener;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PidController implements TrafficShapingController {
    private double expectRt;
    private double targetRatio; // 目标通过率

    protected AtomicLong err = new AtomicLong();
    protected AtomicLong err_last = new AtomicLong();
    protected AtomicLong err_next = new AtomicLong();
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

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-system-status-record-task", true));
    private static SystemStatusListener statusListener = null;

    static {
        statusListener = new SystemStatusListener();
        scheduler.scheduleAtFixedRate(statusListener, 5, 1, TimeUnit.SECONDS);
    }

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
            long newCount = bucketCount.get();

            long previousQps = (long)(node.previousPassQps());
            long previousTotalQps = (long)(node.previousBlockQps()) + previousQps;
            long totalQps = (long)(node.totalQps());
            if (totalQps != 0) {
                // 如果newCount比用户预设的通过率小很多，就让其快速增加
                if (newCount < totalQps * targetRatio / 2) {
                    bucketCount.compareAndSet(newCount, (long)(totalQps * targetRatio / 2));
                }

                long currentCount = bucketCount.get();

                newCount = currentCount + syncCountByPid2();

                bucketCount.compareAndSet(currentCount, newCount);


            }

            long newMaxToken = Math.max(newCount * 5, (long)(previousTotalQps * targetRatio));
            long oldMaxToken = maxTokens.get();
            if (newMaxToken > oldMaxToken) {
                maxTokens.compareAndSet(oldMaxToken, newMaxToken);
            }
            lastCountTime.compareAndSet(oldCountTime, currentTime);
        }
    }

    // 位置式PID控制器
    protected long syncCountByPid() {
        double Kp = 1000, Ki = 5, Kd = 0;
        double errNum = 0.6 - getCurrentCpuUsage();
        //double errNum = expectRt - node.avgRt();
        err.compareAndSet(err.get(), Double.doubleToLongBits(errNum));
        double integralNum = Double.longBitsToDouble(integral.get()) + errNum;
        integral.compareAndSet(integral.get(), Double.doubleToLongBits(integralNum));
        double countNum = Kp * errNum + Ki * integralNum + Kd * (errNum - Double.longBitsToDouble(err_last.get()));
        err_last.compareAndSet(err_last.get(), Double.doubleToLongBits(errNum));
        return (long)countNum;
    }

    // 增量式PID控制器
    protected long syncCountByPid2() {
        double Kp = 1000, Ki = 2.5, Kd = 0;
        double errNum = 0.6 - getCurrentCpuUsage();
        //double errNum = expectRt - node.avgRt();
        double errNextNum = Double.longBitsToDouble(err_next.get());
        double errLastNum = Double .longBitsToDouble(err_last.get());
        err.compareAndSet(err.get(), Double.doubleToLongBits(errNum));
        double incrementNum = Kp * (errNum - errNextNum) + Ki * errNum + Kd * (errNum - 2 * errNextNum + errLastNum);
        err_last.compareAndSet(err_last.get(), Double.doubleToLongBits(errNextNum));
        err_next.compareAndSet(err_next.get(), Double.doubleToLongBits(errNum));
        return (long)incrementNum;
    }

    protected void syncToken(long passQps, long count, long maxToken) {
        long currentTime = TimeUtil.currentTimeMillis();
        currentTime = currentTime - currentTime % 1000;
        long oldLastFillTime = lastFilledTime.get();
        if (currentTime <= oldLastFillTime) {
            return;
        }

        long oldValue = storedTokens.get();
        long newValue = addTokens(currentTime, count, maxToken);

        if (storedTokens.compareAndSet(oldValue, newValue)) {
            long currentValue = storedTokens.addAndGet(0 - passQps);
            if (currentValue < 0) {
                storedTokens.set(0L);
            }
            lastFilledTime.set(currentTime);
        }

    }

    private long addTokens(long currentTime, long count, long maxToken) {
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
        try {
            return statusListener.getCpuUsage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
