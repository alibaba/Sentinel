/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.datasource.redis;

/**
 * Demo1 instance.
 *
 * Note: start local redis server first before start Demo1.
 *
 * @author cdfive
 */
public class RedisDataSourceDemo1 {

    private static final String appName = "RedisDataSourceDemo";

    private static final Integer port = 8701;

    public static void main(String[] args) {
        // Init redis datasource for demo1
        RedisDataSourceDemoUtil.initRedisDataSource(appName, port);

        // Start Flow QPS runner for test
        FlowQpsRunner runner = new FlowQpsRunner(RedisDataSourceDemoUtil.TEST_RESOURCE, 1, 1800);
        runner.simulateTraffic();
        runner.tick();
    }
}
