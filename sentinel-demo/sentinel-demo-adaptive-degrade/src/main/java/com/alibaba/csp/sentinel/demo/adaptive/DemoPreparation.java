package com.alibaba.csp.sentinel.demo.adaptive;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveCircuitBreakerManager;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Preparation for demonstration of adaptive circuit breaker.
 *
 * <p>
 * Due to the need for controlling the request rate per second during the demonstration,
 * there might be times when the effect is not satisfactory.
 * Please try it several times and review the results comprehensively.
 * For more detailed information on the fault situation, please refer to the logs on the local disk.
 * <p>
 *
 * @author ylnxwlp
 */
public class DemoPreparation {

    private static final int THREAD_POOL_SIZE = 200;
    private static final int SIMULATION_DURATION_SECONDS = 15;
    private static final int AWAIT_TERMINATION_MINUTES = 1;

    private static final String RESOURCE_KEY = "adaptive request";

    static void runSimulationWithoutWholeDegradation() throws InterruptedException {
        runSimulation(new ParameterProvider() {
            @Override
            public int getCallsPerSecond(int second) {
                if (second == 4) return 390;
                if (isStressPeriod(second)) return 600;
                if (second == 8) return 360;
                return 300;
            }

            @Override
            public int getErrorFrequency(int second) {
                if (second == 4) return 80;
                if (isStressPeriod(second)) return 30;
                if (second == 8) return 70;
                return 100;
            }

            @Override
            public int getDelayMs(int second) {
                if (second == 4) return 90;
                if (isStressPeriod(second)) return 135;
                if (second == 8) return 105;
                return 50;
            }

            @Override
            public double getCpuUsage(int second) {
                if (isStressPeriod(second)) return 0.75;
                return 0.30;
            }

            private boolean isStressPeriod(int second) {
                return second == 5 || second == 6 || second == 7;
            }
        });
    }

    static void runSimulationWithWholeDegradation() throws InterruptedException {
        runSimulation(new ParameterProvider() {
            @Override
            public int getCallsPerSecond(int second) {
                if (second == 4) return 400;
                if (isStressPeriod(second)) return 600;
                if (second == 8) return 460;
                return 300;
            }

            @Override
            public int getErrorFrequency(int second) {
                if (second == 4) return 80;
                if (isStressPeriod(second)) return 5;
                if (second == 8) return 70;
                return 100;
            }

            @Override
            public int getDelayMs(int second) {
                if (second == 4) return 90;
                if (isStressPeriod(second)) return 200;
                if (second == 8) return 105;
                return 50;
            }

            @Override
            public double getCpuUsage(int second) {
                if (isStressPeriod(second)) return 0.90;
                return 0.30;
            }

            private boolean isStressPeriod(int second) {
                return second == 5 || second == 6 || second == 7;
            }
        });
    }

    private interface ParameterProvider {
        int getCallsPerSecond(int second);

        int getErrorFrequency(int second);

        int getDelayMs(int second);

        double getCpuUsage(int second);
    }

    private static void runSimulation(ParameterProvider provider) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        long startTime = System.currentTimeMillis();

        for (int second = 1; second <= SIMULATION_DURATION_SECONDS; second++) {
            long currentSecond = second;
            int callsPerSecond = provider.getCallsPerSecond(second);
            int errorFrequency = provider.getErrorFrequency(second);
            int delayMs = provider.getDelayMs(second);
            double cpuUsage = provider.getCpuUsage(second);

            System.out.printf("Second %d: Planned calls %d (QPS), exception frequency %.2f calls per exception (%.2f%% error rate), RT %dms%n",
                    second, callsPerSecond,
                    errorFrequency > 0 ? (double) errorFrequency : 0.0,
                    errorFrequency > 0 ? (100.0 / errorFrequency) : 100.0,
                    delayMs);

            for (int i = 0; i < callsPerSecond; i++) {
                final int callIndex = i;
                final int currentDelay = delayMs;
                final double finalCpuUsage = cpuUsage;

                executor.submit(() -> {
                    Entry entry = null;
                    try {
                        entry = SphU.entry(RESOURCE_KEY);
                        AdaptiveServerMetric adaptiveServerMetric = new AdaptiveServerMetric(RESOURCE_KEY);
                        adaptiveServerMetric.setServerCpuUsage(finalCpuUsage);
                        entry.setServerMetric(adaptiveServerMetric);

                        if (currentDelay > 0) {
                            Thread.sleep(currentDelay);
                        }

                        if (errorFrequency > 0 && callIndex % errorFrequency == 0) {
                            entry.setError(new DegradeException("Simulate a production exception"));
                        }

                    } catch (BlockException ex) {
                        RecordLog.warn("block => {}, current CPU usage: {}%, RT: {}ms",
                                ex.getMessage(),
                                String.format("%.1f", cpuUsage),
                                currentDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.printf("Second %d, call %d: Thread interrupted%n",
                                currentSecond, callIndex + 1);
                    } finally {
                        if (entry != null) {
                            entry.exit();
                        }
                    }
                });
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long nextSecondStart = second * 1000L;
            if (elapsed < nextSecondStart) {
                TimeUnit.MILLISECONDS.sleep(nextSecondStart - elapsed);
            }
        }

        executor.shutdown();
        executor.awaitTermination(AWAIT_TERMINATION_MINUTES, TimeUnit.MINUTES);

        System.out.println("\n=== PRODUCTION METRICS SUMMARY ===");
        System.out.println("Adaptive circuit breaker windows for resource '" + RESOURCE_KEY + "':");
        prettyPrintWindows();
        System.out.println("==================================");
        System.out.println("All calls completed");
    }

    private static void prettyPrintWindows() {
        AdaptiveCircuitBreaker cb = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(DemoPreparation.RESOURCE_KEY);
        if (cb == null || cb.getWindows() == null || cb.getWindows().isEmpty()) {
            System.out.println("No window data.");
            return;
        }

        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> sorted = cb.getWindows().stream()
                .sorted(Comparator.comparingLong(WindowWrap::windowStart))
                .collect(Collectors.toList());

        System.out.println("\n=== PER-SECOND METRICS (one line per window) ===");
        System.out.println("Fields: time, QPS, Errors, ErrRate, AvgRT(ms), SumRT(ms)");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV");
        ZoneId zone = ZoneId.systemDefault();

        long totalReq = 0, totalErr = 0, sumRT = 0;

        for (WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> w : sorted) {
            long start = w.windowStart();
            AdaptiveCircuitBreaker.AdaptiveCounter c = w.value();

            long qps = c.getTotalCount().sum();
            long errors = c.getErrorCount().sum();
            long overallRT = c.getOverallRTTime().sum();

            double errRate = qps > 0 ? (errors * 100.0 / qps) : 0.0;
            double avgRT = qps > 0 ? (overallRT * 1.0 / qps) : 0.0;

            totalReq += qps;
            totalErr += errors;
            sumRT += overallRT;

            String ts = Instant.ofEpochMilli(start).atZone(zone).format(fmt);

            String line = String.format(
                    "time=%s | QPS=%d | Errors=%d | ErrRate=%.2f%% | AvgRT(ms)=%.2f | SumRT(ms)=%d",
                    ts, qps, errors, errRate, avgRT, overallRT
            );
            System.out.println(line);
        }

        System.out.println("---- Summary ----");
        double overallErrRate = totalReq > 0 ? (totalErr * 100.0 / totalReq) : 0.0;
        double weightedAvgRT = totalReq > 0 ? (sumRT * 1.0 / totalReq) : 0.0;
        System.out.printf(
                "TotalRequests=%d | TotalErrors=%d | OverallErrRate=%.2f%% | WeightedAvgRT(ms)=%.2f%n",
                totalReq, totalErr, overallErrRate, weightedAvgRT
        );

        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> peakQps = sorted.stream()
                .max(Comparator.comparingLong(w -> w.value().getTotalCount().sum()))
                .orElse(null);
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> worstErr = sorted.stream()
                .max(Comparator.comparingDouble(w -> {
                    long t = w.value().getTotalCount().sum();
                    long e = w.value().getErrorCount().sum();
                    return t > 0 ? (e * 100.0 / t) : 0.0;
                }))
                .orElse(null);
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> slowestAvg = sorted.stream()
                .max(Comparator.comparingDouble(w -> {
                    long t = w.value().getTotalCount().sum();
                    long r = w.value().getOverallRTTime().sum();
                    return t > 0 ? (r * 1.0 / t) : 0.0;
                }))
                .orElse(null);

        Function<WindowWrap<?>, String> tsFmt =
                w -> Instant.ofEpochMilli(w.windowStart()).atZone(zone).format(fmt);

        if (peakQps != null) {
            System.out.printf(
                    "PeakQPS=%d @ %s%n",
                    peakQps.value().getTotalCount().sum(), tsFmt.apply(peakQps)
            );
        }
        if (worstErr != null) {
            long t = worstErr.value().getTotalCount().sum();
            long e = worstErr.value().getErrorCount().sum();
            double er = t > 0 ? (e * 100.0 / t) : 0.0;
            System.out.printf(
                    "WorstErrorRate=%.2f%% (%d/%d) @ %s%n",
                    er, e, t, tsFmt.apply(worstErr)
            );
        }
        if (slowestAvg != null) {
            long t = slowestAvg.value().getTotalCount().sum();
            long r = slowestAvg.value().getOverallRTTime().sum();
            double avg = t > 0 ? (r * 1.0 / t) : 0.0;
            System.out.printf(
                    "SlowestAvgRT(ms)=%.2f @ %s%n",
                    avg, tsFmt.apply(slowestAvg)
            );
        }
    }
}