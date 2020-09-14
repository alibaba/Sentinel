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
package com.alibaba.csp.sentinel.demo.cluster;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Cluster server demo (alone mode).</p>
 * <p>Here we init the cluster server dynamic data sources in
 * {@link com.alibaba.csp.sentinel.demo.cluster.init.DemoClusterServerInitFunc}.</p>
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterServerDemo {
    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ExecutorService pool = Executors.newFixedThreadPool(100);

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ExecutorService monitor = Executors.newFixedThreadPool(1);

    public static void main(String[] args) throws Exception {
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();
        AtomicInteger success = new AtomicInteger(0);
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        ClusterStateManager.setToServer();
        long start = System.currentTimeMillis();

        Runnable monitorTask = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("the concurrency of the rule{flowId:222}: " + CurrentConcurrencyManager.get(222L).get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        monitor.submit(monitorTask);

        for (int i = 0; i < 100; i++) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Entry entry = null;
                    try {
                        entry = SphU.entry("cluster-resource2");
                        System.out.println("pass");
                        success.incrementAndGet();
                    } catch (Exception ex) {
                        System.out.println("block");
                    } finally {
                        countDownLatch.countDown();
                        if (entry != null) {
                            entry.exit();
                        }
                    }
                }
            };
            pool.execute(task);
        }

        countDownLatch.await();
        pool.shutdown();
        System.out.println("the count of pass: " + success.get() + " | time use: " + (System.currentTimeMillis() - start));
    }
}
