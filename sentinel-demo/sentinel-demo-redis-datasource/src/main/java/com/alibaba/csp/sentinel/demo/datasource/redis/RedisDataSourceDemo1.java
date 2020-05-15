package com.alibaba.csp.sentinel.demo.datasource.redis;

/**
 * Start Demo1 instance.
 *
 * @author cdfive
 */
public class RedisDataSourceDemo1 {

    private static final String appName = "RedisDataSourceDemo";

    private static final Integer port = 8701;

    public static void main(String[] args) {
        // Init redis datasource for demo1
        RedisDataSourceDemoUtil.initRedisDataSourec(appName, port);

        // Start Flow QPS runner for test
        FlowQpsRunner runner = new FlowQpsRunner(RedisDataSourceDemoUtil.TEST_RESOURCE, 1, 1800);
        runner.simulateTraffic();
        runner.tick();
    }
}
