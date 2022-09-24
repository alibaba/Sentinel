package com.alibaba.csp.sentinel.demo.datasource.apollo;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * This demo shows how to use Apollo as the data source of Sentinel rules.
 * <br />
 * You need to first set up data as follows:
 * <ol>
 *  <li>Create an application with app id as sentinel-demo in Apollo</li>
 *  <li>
 *    Create a configuration with key as flowRules and value as follows:
 *    <pre>
 *      [
          {
            "resource": "TestResource",
            "controlBehavior": 0,
            "count": 5.0,
            "grade": 1,
            "limitApp": "default",
            "strategy": 0
          }
        ]
 *    </pre>
 *  </li>
 *  <li>Publish the application namespace</li>
 * </ol>
 * Then you could start this demo and adjust the rule configuration as you wish.
 * The rule changes will take effect in real time.
 *
 * @author Jason Song
 */
public class ApolloDataSourceDemo {

    private static final String KEY = "TestResource";

    public static void main(String[] args) {
        loadRules();
        // Assume we config: resource is `TestResource`, initial QPS threshold is 5.
        FlowQpsRunner runner = new FlowQpsRunner(KEY, 1, 100);
        runner.simulateTraffic();
        runner.tick();
    }

    private static void loadRules() {
        // Set up basic information, only for demo purpose. You may adjust them based on your actual environment.
        // For more information, please refer https://github.com/ctripcorp/apollo
        String appId = "sentinel-demo";
        String apolloMetaServerAddress = "http://localhost:8080";
        System.setProperty("app.id", appId);
        System.setProperty("apollo.meta", apolloMetaServerAddress);

        String namespaceName = "application";
        String flowRuleKey = "flowRules";
        // It's better to provide a meaningful default value.
        String defaultFlowRules = "[]";

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ApolloDataSource<>(namespaceName,
            flowRuleKey, defaultFlowRules, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
        }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }
}
