package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveDegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check.AdaptiveDegradeCheck;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check.AdaptiveDegradeCheckProvider;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check.AdaptiveStatisticsConfig;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.Scenario;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.ScenarioConfig;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.ScenarioManager;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util.AdaptiveUtils;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.AbstractCircuitBreaker;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

/**
 * Adaptive Circuit Breaker class.
 *
 * @author ylnxwlp
 */
public class AdaptiveCircuitBreaker extends AbstractCircuitBreaker {

    private final String resourceName;

    private final LeapArray<AdaptiveCounter> stat;

    private volatile Scenario.SystemScenario scenario;

    private volatile double probability;

    private final AdaptiveStatisticsConfig adaptiveStatisticsConfig = AdaptiveStatisticsConfig.getInstance();

    private final AdaptiveProbe adaptiveProbe;

    private volatile AdaptiveServerMetric adaptiveServerMetric;

    public AdaptiveCircuitBreaker(String resourceName) {
        this.resourceName = resourceName;
        this.stat = new AdaptiveLeapArray(adaptiveStatisticsConfig.getSampleCount(), adaptiveStatisticsConfig.getIntervalInMs());
        this.scenario = Scenario.SystemScenario.NORMAL;
        this.adaptiveProbe = AdaptiveProbeManager.getProbe(this);
        this.adaptiveServerMetric = AdaptiveDegradeRuleManager.getServerMetric(resourceName);
    }

    AdaptiveCircuitBreaker(String resourceName, LeapArray<AdaptiveCounter> stat) {
        this.resourceName = resourceName;
        this.stat = stat;
        this.scenario = Scenario.SystemScenario.NORMAL;
        this.adaptiveProbe = AdaptiveProbeManager.getProbe(this);
        this.adaptiveServerMetric = AdaptiveDegradeRuleManager.getServerMetric(resourceName);
    }

    public double getProbability() {
        return probability;
    }

    @Override
    protected void resetStat() {
        stat.currentWindow().value().reset();
    }

    @Override
    public void onRequestComplete(Context context) {
        Entry entry = context.getCurEntry();
        if (entry == null) {
            return;
        }
        adaptiveServerMetric = entry.getServerMetric();
        Throwable error = entry.getError();
        AdaptiveCounter counter = stat.currentWindow().value();
        if (error != null) {
            counter.getErrorCount().add(1);
        }
        counter.getTotalCount().add(1);
        long completeTime = entry.getCompleteTimestamp();
        if (completeTime <= 0) {
            completeTime = TimeUtil.currentTimeMillis();
        }
        long rt = completeTime - entry.getCreateTimestamp();
        counter.getOverallRTTime().add(rt);

        handleStateChangeWithAdaptiveRule(context);
    }

    private void handleStateChangeWithAdaptiveRule(Context context) {
        if (currentState.get() == State.OPEN || currentState.get() == State.THROTTLING) {
            return;
        }
        if (currentState.get() == State.HALF_OPEN) {
            AdaptiveProbe.ProbeResults probeResults = adaptiveProbe.handleProbeRequestOnComplete(context);
            if (probeResults == AdaptiveProbe.ProbeResults.SUCCESS) {
                RecordLog.debug("[AdaptiveCircuitBreaker] resource:{} Half-open state detection successful", resourceName);
                fromHalfOpenToClose();
                scenario = Scenario.SystemScenario.NORMAL;
            }
            if (probeResults == AdaptiveProbe.ProbeResults.FAIL) {
                RecordLog.debug("[AdaptiveCircuitBreaker] resource:{} Half-open state detection fail", resourceName);
                fromHalfOpenToOpen(1.0d);
            }
            return;
        }
        scenario = checkInstability(context.getCurEntry().getServerMetric());
        switch (scenario) {
            case NORMAL:
                break;
            case OVER_LOAD:
                RecordLog.warn("[AdaptiveCircuitBreaker] resource:{} The system has entered an overload state.", resourceName);
                fromCloseToThrottling(1.0d);
                break;
            //TODO Integrate more scenarios
            default:
                RecordLog.error("IsInstability method returns an invalid adaptive scenario");
                break;
        }
    }

    private Scenario.SystemScenario checkInstability(AdaptiveServerMetric serverMetric) {
        WindowWrap<AdaptiveCounter> currentWindow = stat.currentWindow();
        List<WindowWrap<AdaptiveCounter>> historyWindows = stat.list();

        for (Scenario scenario : ScenarioManager.getAllScenarios().values()) {
            if (scenario.matchScenario(resourceName, currentWindow, historyWindows, serverMetric)) {
                return scenario.getScenarioType();
            }
        }
        return Scenario.SystemScenario.NORMAL;
    }

    @Override
    public boolean tryPass(Context context) {
        if (currentState.get() == State.CLOSED) {
            return true;
        }
        if (currentState.get() == State.OPEN) {
            if (retryTimeoutArrived()) {
                double stableQpsAvg = AdaptiveUtils.calculateStableQpsAverage(getWindows(), adaptiveStatisticsConfig);
                int probeCount = (int) Math.round(stableQpsAvg * 0.1);
                adaptiveProbe.setReleaseRequestLimit(Math.max(5, Math.min(20, probeCount)));
                adaptiveProbe.setHalfOpenTimeoutMs(TimeUtil.currentTimeMillis() + ScenarioManager.getConfig(resourceName, scenario).getHalfOpenTimeoutMs());
                return fromOpenToHalfOpenForAdaptive() && adaptiveProbe.handleProbeRequest(context);
            }
        }
        if (currentState.get() == State.HALF_OPEN) {
            return adaptiveProbe.handleProbeRequest(context);
        }
        if (currentState.get() == State.THROTTLING) {
            AdaptiveDegradeCheck arithmetic = AdaptiveDegradeCheckProvider.getInstance();
            AdaptiveCounter counter = stat.currentWindow().value();
            double passProbability = arithmetic.getPassProbability(resourceName, scenario, stat.currentWindow(), stat.list());
            probability = passProbability;
            if (passProbability == -1) {
                RecordLog.error("Invalid adaptive scenario:{}", scenario);
                return true;
            }
            if (passProbability <= 0.05) {
                ScenarioConfig config = ScenarioManager.getConfig(resourceName, scenario);
                int recoveryTimeoutMs = config.getRecoveryTimeoutMs();
                int tomcatQueueSize = adaptiveServerMetric.getServerTomcatQueueSize();
                if (tomcatQueueSize <= 0) {
                    fromThrottlingToOpen(recoveryTimeoutMs, passProbability);
                } else {
                    recoveryTimeoutMs = (int) (tomcatQueueSize * (counter.overallRTTime.sum() / counter.getTotalCount().sum()));
                    fromThrottlingToOpen(recoveryTimeoutMs, passProbability);
                }
                RecordLog.warn("[AdaptiveCircuitBreaker] resource:{} Current request success probability:{},above the threshold, enter the complete degradation mode for {} s.", resourceName, probability, (double) recoveryTimeoutMs / 1000);
                return false;
            } else if (passProbability >= 0.95) {
                fromThrottlingToClose();
                RecordLog.warn("[AdaptiveCircuitBreaker] resource:{} Current request success probability:{},below the threshold, the circuit breaker is closed.", resourceName, probability);
                return true;
            }
            return ThreadLocalRandom.current().nextDouble() < passProbability;
        }
        return false;
    }

    public String getScenario() {
        return scenario.toString();
    }

    public List<WindowWrap<AdaptiveCounter>> getWindows() {
        return stat.list();
    }

    public String getResourceName() {
        return resourceName;
    }

    public static class AdaptiveCounter {
        private LongAdder totalCount;
        private LongAdder errorCount;
        private LongAdder overallRTTime;

        public AdaptiveCounter() {
            this.errorCount = new LongAdder();
            this.totalCount = new LongAdder();
            this.overallRTTime = new LongAdder();
        }

        public LongAdder getErrorCount() {
            return errorCount;
        }

        public LongAdder getTotalCount() {
            return totalCount;
        }

        public LongAdder getOverallRTTime() {
            return overallRTTime;
        }

        public AdaptiveCounter reset() {
            errorCount.reset();
            totalCount.reset();
            overallRTTime.reset();
            return this;
        }

        @Override
        public String toString() {
            return "AdaptiveCounter{" +
                    "totalCount=" + totalCount +
                    ", errorCount=" + errorCount +
                    ", overallRTTime=" + overallRTTime +
                    '}';
        }
    }

    static class AdaptiveLeapArray extends LeapArray<AdaptiveCounter> {

        public AdaptiveLeapArray(int sampleCount, int intervalInMs) {
            super(sampleCount, intervalInMs);
        }

        @Override
        public AdaptiveCounter newEmptyBucket(long timeMillis) {
            return new AdaptiveCounter();
        }

        @Override
        protected WindowWrap<AdaptiveCounter> resetWindowTo(WindowWrap<AdaptiveCounter> w, long startTime) {
            w.resetTo(startTime);
            w.value().reset();
            return w;
        }
    }
}
