package com.alibaba.csp.sentinel.benchmark;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for Sentinel entries.
 *
 * @author yunshu
 */
@Warmup(iterations = 0)
@BenchmarkMode({Mode.AverageTime,Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class SentinelOtherBenchmark {

    @Param({"25", "50", "100", "200", "500", "1000"})
    private int length;

    private List<Integer> numbers;

    @Setup
    public void prepare() {
        numbers = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            numbers.add(ThreadLocalRandom.current().nextInt());
        }
    }

    private void doSomething() {
        Collections.shuffle(numbers);
        Collections.sort(numbers);
    }



    @Benchmark
    @Threads(16)
    public void test16ThreadsTimeUtils() {
        long time = TimeUtil.currentTimeMillis();
    }

    @Benchmark
    @Threads(16)
    public void test16ThreadsSystemTime() {
        long time = System.currentTimeMillis();
    }

    @Benchmark
    @Threads(16)
    public void test16ThreadsSystemNaoTime() {
        long time = System.nanoTime();
    }

    @Benchmark
    @Threads(16)
    public void test16ThreadsSingleEntry() {
        doSomethingWithEntry();
    }

    @Benchmark
    @Threads(16)
    public void test16ThreadsSingleEntry2() {
        doSomethingWithEntry2();
    }

    @Benchmark
    @Threads(16)
    public void test16ThreadsSingleEntry3() {
        doSomethingWithEntry3();
    }


    private void doSomethingWithEntry() {

        Entry interfaceEntry = null;
        try {
            String interfaceName = "interfaceName";
            interfaceEntry = SphU.entry(interfaceName, EntryType.IN);

            doSomething();
        } catch (BlockException e) {

        } finally {
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
        }
    }

    private void doSomethingWithEntry2() {

        Entry interfaceEntry = null;
        Entry methodEntry = null;
        try {

            String resourceName = "resourceName";
            String interfaceName = "interfaceName";


            interfaceEntry = SphU.entry(interfaceName, EntryType.IN);
            methodEntry = SphU.entry(resourceName, EntryType.IN, 1);

            doSomething();
        } catch (BlockException e) {

        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1);
            }
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }


        }
    }


    private void doSomethingWithEntry3() {
        Entry projectEntry = null;
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        try {
            String projectName = "projectName";
            String resourceName = "resourceName";
            String interfaceName = "interfaceName";

            projectEntry = SphU.entry(projectName, EntryType.IN);
            interfaceEntry = SphU.entry(interfaceName, EntryType.IN);
            methodEntry = SphU.entry(resourceName, EntryType.IN, 1);

            doSomething();
        } catch (BlockException e) {

        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1);
            }
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
            if (projectEntry != null) {
                projectEntry.exit();
            }

        }
    }
}

