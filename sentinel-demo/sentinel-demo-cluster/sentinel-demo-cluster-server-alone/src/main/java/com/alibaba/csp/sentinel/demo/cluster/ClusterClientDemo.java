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
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.slots.block.flow.timeout.ReSourceTimeoutStrategyUtil;

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
public class ClusterClientDemo {
    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ExecutorService pool = Executors.newFixedThreadPool(1000);

    public static void main(String[] args) throws Exception {
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();
        AtomicInteger success = new AtomicInteger(0);
        final CountDownLatch countDownLatch = new CountDownLatch(10000000);
        ClusterStateManager.setToClient();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Entry entry = null;
                    try {
                        entry = SphU.entry("cluster-resource2");
//                        System.out.println("pass");
                        success.incrementAndGet();
//                        Thread.sleep(100000);
                    } catch (Exception ex) {
//                        System.out.println("block");
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
        System.out.println("请求通过" + success.get() + "|存储大小" + ReSourceTimeoutStrategyUtil.getTimeoutSize() + "|请求用时" + (System.currentTimeMillis() - start));
        Thread.sleep(10000);
        System.out.println("请求通过" + success.get() + "|存储大小" + ReSourceTimeoutStrategyUtil.getTimeoutSize() + "|请求用时" + (System.currentTimeMillis() - start));
    }
}
