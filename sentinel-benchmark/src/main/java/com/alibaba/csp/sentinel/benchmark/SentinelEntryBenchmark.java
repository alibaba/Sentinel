/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark for Sentinel entries.
 *
 * @author Eric Zhao
 */
@Warmup(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class SentinelEntryBenchmark {

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

    private void doSomethingWithEntry() {
        Entry e0 = null;
        try {
            e0 = SphU.entry("benchmark");
            doSomething();
        } catch (BlockException e) {
        } finally {
            if (e0 != null) {
                e0.exit();
            }
        }
    }

    @Benchmark
    @Threads(1)
    public void testSingleThreadDirectly() {
        doSomething();
    }

    @Benchmark
    @Threads(1)
    public void testSingleThreadSingleEntry() {
        doSomethingWithEntry();
    }

    @Benchmark
    @Threads(2)
    public void test2ThreadsSingleEntry() {
        doSomethingWithEntry();
    }

    @Benchmark
    @Threads(3)
    public void test3ThreadsSingleEntry() {
        doSomethingWithEntry();
    }

    @Benchmark
    @Threads(4)
    public void test4ThreadsDirectly() {
        doSomething();
    }

    @Benchmark
    @Threads(4)
    public void test4ThreadsSingleEntry() {
        doSomethingWithEntry();
    }

    @Benchmark
    @Threads(8)
    public void test8ThreadsDirectly() {
        doSomething();
    }

    @Benchmark
    @Threads(8)
    public void test8ThreadsSingleEntry() {
        doSomethingWithEntry();
    }

    @Benchmark
    @Threads(16)
    public void test16ThreadsDirectly() {
        doSomething();
    }

    @Benchmark
    @Threads(16)
    public void test16ThreadsSingleEntry() {
        doSomethingWithEntry();
    }
}
