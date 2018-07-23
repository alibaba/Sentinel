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
package com.alibaba.csp.sentinel.demo.file.rule;

import java.util.List;

import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.datasource.DataSource;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.demo.file.rule.parser.JsonDegradeRuleListParser;
import com.alibaba.csp.sentinel.demo.file.rule.parser.JsonFlowRuleListParser;
import com.alibaba.csp.sentinel.demo.file.rule.parser.JsonSystemRuleListParser;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

/**
 * <p>
 * This Demo shows how to use {@link FileRefreshableDataSource} to read {@link Rule}s from file. The
 * {@link FileRefreshableDataSource} will automatically fetches the backend file every 3 seconds, and
 * inform the listener if the file is updated.
 * </p>
 * <p>
 * Each {@link DataSource} has a {@link SentinelProperty} to hold the deserialized config data.
 * {@link PropertyListener} will listen to the {@link SentinelProperty} instead of the datasource.
 * {@link ConfigParser} is used for telling how to deserialize the data.
 * </p>
 * <p>
 * {@link FlowRuleManager#register2Property(SentinelProperty)},
 * {@link DegradeRuleManager#register2Property(SentinelProperty)},
 * {@link SystemRuleManager#register2Property(SentinelProperty)} could be called for listening the
 * {@link Rule}s change.
 * </p>
 * <p>
 * For other kinds of data source, such as <a href="https://github.com/alibaba/nacos">Nacos</a>,
 * Zookeeper, Git, or even CSV file, We could implement {@link DataSource} interface to read these
 * configs.
 * </p>
 *
 * @author Carpenter Lee
 */
public class FileDataSourceDemo {

    public static void main(String[] args) throws Exception {
        FileDataSourceDemo demo = new FileDataSourceDemo();
        demo.listenRules();

        /**
         * Start to require tokens, rate will be limited by rule in FlowRule.json
         */
        FlowQpsRunner runner = new FlowQpsRunner();
        runner.simulateTraffic();
        runner.tick();
    }

    public void listenRules() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String flowRulePath = classLoader.getResource("FlowRule.json").getFile();
        String degradeRulePath = classLoader.getResource("DegradeRule.json").getFile();
        String systemRulePath = classLoader.getResource("SystemRule.json").getFile();

        // data source for FlowRule
        DataSource<String, List<FlowRule>> flowRuleDataSource = new FileRefreshableDataSource<List<FlowRule>>(
            flowRulePath, new JsonFlowRuleListParser());
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        // data source for DegradeRule
        DataSource<String, List<DegradeRule>> degradeRuleDataSource = new FileRefreshableDataSource<List<DegradeRule>>(
            degradeRulePath, new JsonDegradeRuleListParser());
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());

        // data source for SystemRule
        DataSource<String, List<SystemRule>> systemRuleDataSource = new FileRefreshableDataSource<List<SystemRule>>(
            systemRulePath, new JsonSystemRuleListParser());
        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
    }
}
