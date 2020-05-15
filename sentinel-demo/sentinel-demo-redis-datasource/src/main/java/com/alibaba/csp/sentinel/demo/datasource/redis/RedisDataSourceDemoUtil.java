package com.alibaba.csp.sentinel.demo.datasource.redis;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * @author cdfive
 */
public class RedisDataSourceDemoUtil {

    public static final String TEST_RESOURCE = "test_resource";

    public static final String redisHost = "localhost";

    public static final Integer redisPort = 6379;

    public static final String dashboardServer = "localhost:8080";

    public static void initRedisDataSource(String appName, Integer port) {
        // Set app name
        System.setProperty(SentinelConfig.APP_NAME_PROP_KEY, appName);
        // Set dashboard server address
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, dashboardServer);
        // Get client ip
        String ip = TransportConfig.getHeartbeatClientIp();
        // Set transport port
        TransportConfig.setRuntimePort(port);

        // Init redis connection config
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.builder().withHost(redisHost).withPort(redisPort).build();

        // Init RedisDataSource for flow rules
        String flowRuleKey = RedisDataSourceDemoUtil.join("-", appName, ip, String.valueOf(port), "flow", "rules");
        RedisDataSource<List<FlowRule>> flowRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, flowRuleKey, flowRuleKey, new Converter<String, List<FlowRule>>() {
            @Override
            public List<FlowRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
            }
        });
        FlowRuleManager.register2Property(flowRuleRedisDataSource.getProperty());

        // Init RedisDataSource for degrade rules
        String degradeRuleKey = RedisDataSourceDemoUtil.join("-", appName, ip, String.valueOf(port), "degrade", "rules");
        RedisDataSource<List<DegradeRule>> degradeRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, degradeRuleKey, degradeRuleKey, new Converter<String, List<DegradeRule>>() {
            @Override
            public List<DegradeRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {});
            }
        });
        DegradeRuleManager.register2Property(degradeRuleRedisDataSource.getProperty());

        // Init RedisDataSource for system rules
        String systemRuleKey = RedisDataSourceDemoUtil.join("-", appName, ip, String.valueOf(port), "system", "rules");
        RedisDataSource<List<SystemRule>> systemRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, systemRuleKey, systemRuleKey, new Converter<String, List<SystemRule>>() {
            @Override
            public List<SystemRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<SystemRule>>() {});
            }
        });
        SystemRuleManager.register2Property(systemRuleRedisDataSource.getProperty());

        // Init RedisDataSource for authority rules
        String authorityRuleKey = RedisDataSourceDemoUtil.join("-", appName, ip, String.valueOf(port), "authority", "rules");
        RedisDataSource<List<AuthorityRule>> authorityRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, authorityRuleKey, authorityRuleKey, new Converter<String, List<AuthorityRule>>() {
            @Override
            public List<AuthorityRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {});
            }
        });
        AuthorityRuleManager.register2Property(authorityRuleRedisDataSource.getProperty());

        // Init RedisDataSource for paramFlow rules
        String paramFlowRuleKey = RedisDataSourceDemoUtil.join("-", appName, ip, String.valueOf(port), "paramFlow", "rules");
        RedisDataSource<List<ParamFlowRule>> paramFlowRuleRedisDataSource = new RedisDataSource<>(redisConnectionConfig, paramFlowRuleKey, paramFlowRuleKey, new Converter<String, List<ParamFlowRule>>() {
            @Override
            public List<ParamFlowRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {});
            }
        });
        ParamFlowRuleManager.register2Property(paramFlowRuleRedisDataSource.getProperty());
    }

    public static String join(String separator, String ... values) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String value : values) {
            if (!first) {
                sb.append(separator);
            } else {
                first = false;
            }
            sb.append(value);
        }
        return sb.toString();
    }
}
