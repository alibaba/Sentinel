package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check.AdaptiveStatisticsConfig;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.OverloadScenarioConfig;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.Scenario;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.ScenarioManager;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A tool class for adaptive circuit breaking. It mainly performs mathematical calculations.
 *
 * @author ylnxwlp
 */
public final class AdaptiveUtils {

    private static final int WINDOW_COUNT = 20;
    private static final double EPS = 1e-12;
    private static final double JEFFREYS_A = 0.5;
    private static final double JEFFREYS_B = 0.5;
    private static final double MAD_TO_SIGMA = 1.4826;
    private static final double EDGE_PROBABILITY = 0.8;
    private static final double MIN_TAU = 1.0 / WINDOW_COUNT;

    private AdaptiveUtils() {
    }

    public static boolean isProbeSuccess(int successCount,
                                         int failureCount,
                                         List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
        if (failureCount == 0) {
            return true;
        }
        if (successCount == 0) {
            return false;
        }

        int totalProbe = successCount + failureCount;
        if (totalProbe < 5) {
            return false;
        }

        double currentErrorRate = (double) failureCount / totalProbe;

        List<Double> rawErrorRates = extractErrorRates(windows);
        if (rawErrorRates.isEmpty()) {
            return currentErrorRate <= 0.15;
        }

        List<Double> stableErrorRates = filterOutliers(rawErrorRates);
        if (stableErrorRates.isEmpty()) {
            return currentErrorRate <= 0.15;
        }

        MeanStdDev msd = calculateMeanAndStdDev(stableErrorRates);
        double threshold = msd.mean + 2 * msd.stdDev;

        threshold = Math.min(1.0, Math.max(threshold, 0.05));
        return currentErrorRate <= threshold;
    }

    public static List<Double> extractErrorRates(List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
        if (windows == null || windows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Double> out = new ArrayList<>(windows.size());
        for (WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> win : windows) {
            if (win == null || win.value() == null) {
                continue;
            }
            AdaptiveCircuitBreaker.AdaptiveCounter c = win.value();
            long total = c.getTotalCount().sum();
            long err = c.getErrorCount().sum();
            if (total > 0) {
                out.add((double) err / (double) total);
            }
        }
        return out;
    }

    public static MeanStdDev calculateMeanAndStdDev(List<Double> data) {
        if (data == null || data.isEmpty()) {
            return new MeanStdDev(0.0, 0.0);
        }
        double sum = 0.0;
        for (Double d : data) {
            if (d != null) {
                sum += d;
            }
        }
        double mean = sum / data.size();

        double acc = 0.0;
        for (Double d : data) {
            if (d != null) {
                double diff = d - mean;
                acc += diff * diff;
            }
        }
        double variance = acc / data.size();
        return new MeanStdDev(mean, Math.sqrt(variance));
    }

    public static final class MeanStdDev {
        public final double mean;
        public final double stdDev;

        public MeanStdDev(double mean, double stdDev) {
            this.mean = mean;
            this.stdDev = stdDev;
        }
    }

    public static double getPassProbabilityWhenOverloading(
            String resourceName,
            WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
            List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {

        if (currentWindow == null || windows == null || windows.isEmpty()) {
            return 1.0;
        }

        OverloadScenarioConfig config =
                (OverloadScenarioConfig) ScenarioManager.getConfig(resourceName, Scenario.SystemScenario.OVER_LOAD);
        double rtMultiplier = config.getResponseTimeMultiple();
        double erMultiplier = config.getErrorRateMultiple();

        int nWin = windows.size();
        double[] avgRt = new double[nWin];
        double[] pErr = new double[nWin];
        long[] nTotal = new long[nWin];
        long[] nErr = new long[nWin];

        int currentIdx = -1;
        for (int i = 0; i < nWin; i++) {
            WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> win = windows.get(i);
            if (win == currentWindow) {
                currentIdx = i;
            }
            AdaptiveCircuitBreaker.AdaptiveCounter c = win.value();
            long total = safeLong(c.getTotalCount().sum());
            long err = safeLong(c.getErrorCount().sum());
            double rt = safeDouble(c.getOverallRTTime().sum());
            nTotal[i] = Math.max(0L, total);
            nErr[i] = Math.max(0L, err);
            avgRt[i] = nTotal[i] > 0 ? rt / (double) nTotal[i] : 0.0;
            pErr[i] = (nErr[i] + JEFFREYS_A) / (nTotal[i] + JEFFREYS_A + JEFFREYS_B);
        }
        if (currentIdx < 0) {
            currentIdx = nWin - 1;
        }

        DoubleBuffer rtHist = new DoubleBuffer(nWin - 1);
        DoubleBuffer pHist = new DoubleBuffer(nWin - 1);
        long sumTotalHist = 0L, sumErrHist = 0L;
        for (int i = 0; i < nWin; i++) {
            if (i == currentIdx) {
                continue;
            }
            rtHist.add(avgRt[i]);
            pHist.add(pErr[i]);
            sumTotalHist += nTotal[i];
            sumErrHist += nErr[i];
        }
        if (sumTotalHist == 0L) {
            return 1.0;
        }

        double rtMedian = median(rtHist.toArray());
        double pMedian = median(pHist.toArray());
        double pOverall = (sumErrHist + JEFFREYS_A) / (sumTotalHist + JEFFREYS_A + JEFFREYS_B);
        double pBar = Math.max(pMedian, pOverall);

        double sigmaRt = MAD_TO_SIGMA * mad(rtHist.toArray(), rtMedian);
        double sigmaP = MAD_TO_SIGMA * mad(pHist.toArray(), pMedian);

        double denomRt = Math.max(rtMedian, rtMedian / WINDOW_COUNT + EPS);
        double denomP = Math.max(pBar, 1.0 / (sumTotalHist + 1.0));

        double cvRt = denomRt > 0.0 ? sigmaRt / denomRt : 0.0;
        double cvP = denomP > 0.0 ? sigmaP / denomP : 0.0;

        double wRt = 1.0 / (1.0 + cvRt * cvRt);
        double wP = 1.0 / (1.0 + cvP * cvP);

        double thetaRt = rtMultiplier * rtMedian;
        double thetaP = erMultiplier * pBar;

        double xRt = poslog(safeRatio(avgRt[currentIdx], thetaRt));
        double xP = poslog(safeRatio(pErr[currentIdx], thetaP));

        double s = hypot(wRt * xRt, wP * xP);

        DoubleBuffer sHist = new DoubleBuffer(nWin - 1);
        for (int i = 0; i < nWin; i++) {
            if (i == currentIdx) {
                continue;
            }
            double xiRt = poslog(safeRatio(avgRt[i], thetaRt));
            double xiP = poslog(safeRatio(pErr[i], thetaP));
            sHist.add(hypot(wRt * xiRt, wP * xiP));
        }

        double tau = Math.max(percentile95(sHist.toArray()), MIN_TAU);
        long nCur = nTotal[currentIdx];
        if (nCur == 0L) {
            return 1.0;
        }

        double tauSafe = Math.max(tau, EPS);
        double s0 = tauSafe * Math.sqrt(Math.max(1.0 / Math.max(EDGE_PROBABILITY, 1e-6) - 1.0, 0.0));
        double ratio = (s + s0) / tauSafe;
        double pass = 1.0 / (1.0 + ratio * ratio);
        if (Double.isNaN(pass) || Double.isInfinite(pass)) {
            pass = 1.0;
        }
        if (pass < 0.0) {
            pass = 0.0;
        }
        if (pass > 1.0) {
            pass = 1.0;
        }
        return pass;
    }

    public static String packServerMetric() {
        double currentCpuUsage = SystemRuleManager.getCurrentCpuUsage();
        //TODO fully integrated Tomcat thread pool monitoring mechanism
        return String.format("cpu:%.2f,tomcatQueue:-1,tomcatUsage:-1",
                currentCpuUsage);
    }

    public static AdaptiveServerMetric parseServiceMetrics(String metricHeader, String name) {
        if (metricHeader == null || metricHeader.trim().isEmpty()) {
            return new AdaptiveServerMetric(name);
        }
        AdaptiveServerMetric metric = new AdaptiveServerMetric(name);
        String[] parts = metricHeader.split(",");
        for (String part : parts) {
            if (part.startsWith("cpu:")) {
                try {
                    String value = part.substring(4);
                    double cpuUsage = Double.parseDouble(value);
                    metric.setServerCpuUsage(cpuUsage);
                } catch (NumberFormatException ignore) {
                }
            } else if (part.startsWith("tomcatQueue:")) {
                try {
                    String value = part.substring(12);
                    int queueSize = Integer.parseInt(value);
                    metric.setServerTomcatQueueSize(queueSize);
                } catch (NumberFormatException ignore) {
                }
            } else if (part.startsWith("tomcatUsage:")) {
                try {
                    String value = part.substring(12);
                    double usageRate = Double.parseDouble(value);
                    metric.setServerTomcatUsageRate(usageRate);
                } catch (NumberFormatException ignore) {
                }
            }
        }
        return metric;
    }

    public static double calculateStableQpsAverage(
            List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows,
            AdaptiveStatisticsConfig adaptiveStatisticsConfig) {

        if (windows == null || windows.isEmpty()) {
            RecordLog.debug("[AdaptiveUtils] No history windows for stable qps average calculation");
            return 0.0;
        }

        long windowMs = getSingleWindowMs(adaptiveStatisticsConfig);
        List<Double> windowQpsList = new ArrayList<>();

        for (WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> window : windows) {
            if (window == null || window.value() == null) {
                continue;
            }
            long totalCount = window.value().getTotalCount().sum();
            if (totalCount <= 0) {
                continue;
            }
            double windowQps = totalCount / (windowMs / 1000.0);
            windowQpsList.add(windowQps);
        }

        if (windowQpsList.isEmpty()) {
            RecordLog.debug("[AdaptiveUtils] No valid window QPS data");
            return 0.0;
        }

        List<Double> stableQpsList = filterOutliers(windowQpsList);
        if (stableQpsList.isEmpty()) {
            RecordLog.debug("[AdaptiveUtils] All QPS data are outliers, use original average");
            stableQpsList = windowQpsList;
        }

        return stableQpsList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static long getSingleWindowMs(AdaptiveStatisticsConfig adaptiveStatisticsConfig) {
        int intervalInMs = adaptiveStatisticsConfig.getIntervalInMs();
        int sampleCount = adaptiveStatisticsConfig.getSampleCount();
        if (sampleCount <= 0) {
            RecordLog.warn("[AdaptiveUtils] AdaptiveStatisticsConfig sampleCount is invalid:{},use default 100ms window", sampleCount);
            return 100;
        }
        return intervalInMs / sampleCount;
    }

    private static List<Double> filterOutliers(List<Double> data) {
        if (data.size() <= 1) {
            return data;
        }
        List<Double> sorted = new ArrayList<>(data);
        Collections.sort(sorted);
        double q1 = getMedianDouble(sorted.subList(0, sorted.size() / 2));
        double q3 = sorted.size() % 2 == 0
                ? getMedianDouble(sorted.subList(sorted.size() / 2, sorted.size()))
                : getMedianDouble(sorted.subList(sorted.size() / 2 + 1, sorted.size()));
        double iqr = q3 - q1;
        double lower = q1 - 1.5 * iqr;
        double upper = q3 + 1.5 * iqr;
        return data.stream()
                .filter(v -> v != null && v >= Math.max(0, lower) && v <= upper)
                .collect(Collectors.toList());
    }

    private static double getMedianDouble(List<Double> data) {
        if (data.isEmpty()) {
            return 0.0;
        }
        int size = data.size();
        if ((size & 1) == 1) {
            return data.get(size / 2);
        }
        return (data.get(size / 2 - 1) + data.get(size / 2)) / 2.0;
    }

    private static long safeLong(long x) {
        return Math.max(0L, x);
    }

    private static double safeDouble(double x) {
        return Math.max(0.0, x);
    }

    private static double safeRatio(double num, double den) {
        return den > 1.0E-12 ? num / den : (num > 0.0 ? Double.POSITIVE_INFINITY : 0.0);
    }

    private static double poslog(double x) {
        if (!(x > 1.0)) {
            return 0.0;
        }
        return Math.log(x);
    }

    private static double hypot(double a, double b) {
        return Math.hypot(a, b);
    }

    private static double median(double[] a) {
        if (a == null || a.length == 0) {
            return 0.0;
        }
        double[] b = a.clone();
        java.util.Arrays.sort(b);
        int n = b.length;
        if ((n & 1) == 1) {
            return b[n >> 1];
        }
        return 0.5 * (b[(n >> 1) - 1] + b[n >> 1]);
    }

    private static double mad(double[] a, double center) {
        if (a == null || a.length == 0) {
            return 0.0;
        }
        double[] dev = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            dev[i] = Math.abs(a[i] - center);
        }
        return median(dev);
    }

    private static double percentile95(double[] a) {
        if (a == null || a.length == 0) {
            return 0.0;
        }
        double[] b = a.clone();
        java.util.Arrays.sort(b);
        int n = b.length;
        int idx = (int) Math.ceil(0.95 * n) - 1;
        if (idx < 0) {
            idx = 0;
        }
        if (idx >= n) {
            idx = n - 1;
        }
        return b[idx];
    }

    private static final class DoubleBuffer {
        private double[] buf;
        private int size;

        DoubleBuffer(int cap) {
            this.buf = new double[Math.max(cap, 4)];
        }

        void add(double v) {
            if (size == buf.length) {
                double[] nb = new double[buf.length << 1];
                System.arraycopy(buf, 0, nb, 0, buf.length);
                buf = nb;
            }
            buf[size++] = v;
        }

        double[] toArray() {
            double[] out = new double[size];
            System.arraycopy(buf, 0, out, 0, size);
            return out;
        }
    }
}
