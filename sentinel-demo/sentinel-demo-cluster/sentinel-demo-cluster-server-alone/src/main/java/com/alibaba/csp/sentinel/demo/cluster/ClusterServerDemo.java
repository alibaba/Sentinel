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

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNodeManager;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static ExecutorService pool = Executors.newFixedThreadPool(1);

    public static void main(String[] args) throws Exception {
        // Not embedded mode by default (alone mode).
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();
        ClusterStateManager.setToServer();
        while (true) {
            Thread.sleep(1000);
            try{
                System.out.println("|资源:resource2当前并发量" + CurrentConcurrencyManager.get(222L).get() + "|存储token数" + TokenCacheNodeManager.getSize());
            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }
}

//java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.7.2.jar
//curl http://localhost:8719/cluster/server/flowRules
//   1.功能演示
//   2.关于pr(希望尽快review)
//   3.一些问题
//   4.未来规划
//     下一个pr是通信部分
//     最后一个pr是slot部分接入并发集群流控
//resourceTimeoutStrategy 失败
//        blockStrategy 有问题
//

//server     ---------------
//改进：
//阻塞队列 ，，出方案
//异步释放 ，，，√
//本地流控 ，，出方案
//并发流控controller规则校验√
//client     ---------------