package com.alibaba.csp.sentinel.demo.datasource.redis;

/**
 * Demo1 instance.
 *
 * Note: start local redis-server first before start Demo1.
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
