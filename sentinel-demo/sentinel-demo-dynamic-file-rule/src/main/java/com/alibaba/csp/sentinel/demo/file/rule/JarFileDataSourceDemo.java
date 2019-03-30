package com.alibaba.csp.sentinel.demo.file.rule;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.JarFileRefreshableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * @author dq
 * @date 2019-03-30
 * @desc 描述
 */
public class JarFileDataSourceDemo {
    public static void main(String[] args) throws Exception {
        JarFileDataSourceDemo demo = new JarFileDataSourceDemo();
        demo.listenRules();

        /*
         * Start to require tokens, rate will be limited by rule in FlowRule.json
         */
        FlowQpsRunner runner = new FlowQpsRunner();
        runner.simulateTraffic();
        runner.tick();
    }

    private void listenRules() throws Exception {
        String jarPath = System.getProperty("user.dir") + "/sentinel-demo/sentinel-demo-dynamic-file-rule/target/sentinel-demo-dynamic-file-rule-1.5.1-SNAPSHOT.jar";
        // eg: if flowRuleInJarName full path is 'sentinel-demo-dynamic-file-rule-1.5.1-SNAPSHOT.jar!/classes/FlowRule.json',
        // your flowRuleInJarName is 'classes/FlowRule.json'
        String flowRuleInJarPath = "FlowRule.json";

        JarFileRefreshableDataSource<List<FlowRule>> flowRuleDataSource = new JarFileRefreshableDataSource<>(
                jarPath,flowRuleInJarPath, flowRuleListParser);
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    private Converter<String, List<FlowRule>> flowRuleListParser = source -> JSON.parseObject(source,
            new TypeReference<List<FlowRule>>() {});
}
