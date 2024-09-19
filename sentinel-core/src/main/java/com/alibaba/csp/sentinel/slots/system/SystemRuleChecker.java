package com.alibaba.csp.sentinel.slots.system;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



/**
 * <p>
 * Sentinel System Rule makes the inbound traffic and capacity meet. It takes
 * average rt, qps, thread count of incoming requests into account. And it also
 * provides a measurement of system's load, but only available on Linux.
 * </p>
 * <p>
 * rt, qps, thread count is easy to understand. If the incoming requests'
 * rt,qps, thread count exceeds its threshold, the requests will be
 * rejected.however, we use a different method to calculate the load.
 * </p>
 * <p>
 * Consider the system as a pipeline，transitions between constraints result in
 * three different regions (traffic-limited, capacity-limited and danger area)
 * with qualitatively different behavior. When there isn’t enough request in
 * flight to fill the pipe, RTprop determines behavior; otherwise, the system
 * capacity dominates. Constraint lines intersect at inflight = Capacity ×
 * RTprop. Since the pipe is full past this point, the inflight –capacity excess
 * creates a queue, which results in the linear dependence of RTT on inflight
 * traffic and an increase in system load.In danger area, system will stop
 * responding.<br/>
 * Referring to BBR algorithm to learn more.
 * </p>
 * <p>
 * Note that {@link SystemRule} only effect on inbound requests, outbound traffic
 * will not limit by {@link SystemRule}
 * </p>
 *
 * @author jialiang.linjl
 * @author leyou
 * @author guozhong.huang
 */
public class SystemRuleChecker {

    private static SystemStatusListener statusListener = null;
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-system-status-record-task", true));

    static {
        statusListener = new SystemStatusListener();
        scheduler.scheduleAtFixedRate(statusListener, 0, 1, TimeUnit.SECONDS);
    }

    public static void checkSystem(Collection<SystemRule> rules, ResourceWrapper resourceWrapper, int count) throws SystemBlockException {
        if (resourceWrapper.getEntryType() != EntryType.IN) {
            return;
        }
        if (rules != null) {
            for (SystemRule rule : rules) {
                if (!canPassCheck(rule, count)) {
                    throw new SystemBlockException(resourceWrapper.getName(), rule);
                }
            }
        }
    }


    public static boolean canPassCheck(SystemRule rule, int acquireCount) {
        SystemMetricType systemMetricType = rule.getSystemMetricType();
        double threshold = rule.getTriggerCount();
        switch (systemMetricType) {
            case LOAD:
                double currentSystemAvgLoad = getCurrentSystemAvgLoad();
                if (currentSystemAvgLoad > threshold) {
                    return checkBbr();
                }
                return true;
            case AVG_RT:
                double rt = Constants.ENTRY_NODE.avgRt();
                return rt <= threshold ;
            case CONCURRENCY:
                int currentThread = Constants.ENTRY_NODE.curThreadNum();
                return currentThread <= threshold;
            case INBOUND_QPS:
                double currentQps = Constants.ENTRY_NODE.passQps();
                return currentQps + acquireCount <= threshold;
            case CPU_USAGE:
                double currentCpuUsage = getCurrentCpuUsage();
                return currentCpuUsage <= threshold;
            default:
                return true;
        }
    }


    private static boolean checkBbr() {
        int currentThread = Constants.ENTRY_NODE.curThreadNum();
        if (currentThread > 1 &&
                currentThread > Constants.ENTRY_NODE.maxSuccessQps() * Constants.ENTRY_NODE.minRt() / 1000) {
            return false;
        }
        return true;
    }

    public static double getCurrentSystemAvgLoad() {
        return statusListener.getSystemAverageLoad();
    }

    public static double getCurrentCpuUsage() {
        return statusListener.getCpuUsage();
    }
}
