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
package com.alibaba.csp.sentinel.slots.block.degrade.cb;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

/**
 * A {@link CircuitBreaker} based on average response time.
 *
 * @author Eric Zhao
 */
public class RtCircuitBreaker implements CircuitBreaker {

    private static final int RT_MAX_EXCEED_N = 5;

    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("sentinel-degrade-reset-task", true));

    private final DegradeRule rule;
    private final AtomicBoolean cut = new AtomicBoolean(false);
    private final AtomicLong passCount = new AtomicLong(0);

    public RtCircuitBreaker(DegradeRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Bad degrade rule");
        }
        this.rule = rule;
    }

    @Override
    public boolean canPass() {
        if (cut.get()) {
            return false;
        }

        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(rule.getResource());
        if (clusterNode == null) {
            return true;
        }

        double rt = clusterNode.avgRt();
        if (rt < rule.getCount()) {
            passCount.set(0);
            return true;
        }

        // Sentinel will degrade the service only if count exceeds.
        if (passCount.incrementAndGet() < RT_MAX_EXCEED_N) {
            return true;
        }

        tryOpenCircuit();

        return false;
    }

    private void tryOpenCircuit() {
        if (cut.compareAndSet(false, true)) {
            Runnable resetTask = new Runnable() {
                @Override
                public void run() {
                    passCount.set(0);
                    cut.compareAndSet(true, false);
                }
            };
            pool.schedule(resetTask, rule.getTimeWindow(), TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isCut() {
        return cut.get();
    }

    @Override
    public DegradeRule getRule() {
        return this.rule;
    }
}
