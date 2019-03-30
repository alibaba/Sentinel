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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * <p>
 * This Demo shows how to use {@link FileRefreshableDataSource} to read {@link Rule}s from file. The
 * {@link FileRefreshableDataSource} will automatically fetches the backend file every 3 seconds, and
 * inform the listener if the file is updated.
 * </p>
 * <p>
 * Each {@link ReadableDataSource} has a {@link SentinelProperty} to hold the deserialized config data.
 * {@link PropertyListener} will listen to the {@link SentinelProperty} instead of the datasource.
 * {@link Converter} is used for telling how to deserialize the data.
 * </p>
 * <p>
 * {@link FlowRuleManager#register2Property(SentinelProperty)},
 * {@link DegradeRuleManager#register2Property(SentinelProperty)},
 * {@link SystemRuleManager#register2Property(SentinelProperty)} could be called for listening the
 * {@link Rule}s change.
 * </p>
 * <p>
 * For other kinds of data source, such as <a href="https://github.com/alibaba/nacos">Nacos</a>,
 * Zookeeper, Git, or even CSV file, We could implement {@link ReadableDataSource} interface to read these
 * configs.
 * </p>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
public class FileDataSourceDemo {

    public static void main(String[] args) throws Exception {
        FileDataSourceDemo demo = new FileDataSourceDemo();
        demo.listenRules();

        /*
         * Start to require tokens, rate will be limited by rule in FlowRule.json
         */
        FlowQpsRunner runner = new FlowQpsRunner();
        runner.simulateTraffic();
        runner.tick();
    }

    private void listenRules() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
//        String flowRulePath = URLDecoder.decode(classLoader.getResource("FlowRule.json").getFile(), "UTF-8");
//        String degradeRulePath = URLDecoder.decode(classLoader.getResource("DegradeRule.json").getFile(), "UTF-8");
//        String systemRulePath = URLDecoder.decode(classLoader.getResource("SystemRule.json").getFile(), "UTF-8");

        // Data source in JarFile for FlowRule
//        String flowRuleInJarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        String flowRuleJarPath = System.getProperty("user.dir")+"/sentinel-demo/sentinel-demo-dynamic-file-rule/target/sentinel-demo-dynamic-file-rule-1.5.1-SNAPSHOT.jar";
        String flowRuleInJarPath = "FlowRule.json";
        FileRefreshableDataSource<List<FlowRule>> flowRuleDataSourceInJarFile = new FileRefreshableDataSource<>(
                flowRuleJarPath, flowRuleInJarPath, flowRuleListParser);
        FlowRuleManager.register2Property(flowRuleDataSourceInJarFile.getProperty());

        // Data source for FlowRule
//        FileRefreshableDataSource<List<FlowRule>> flowRuleDataSource = new FileRefreshableDataSource<>(
//                flowRulePath, flowRuleListParser);
//        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        // Data source for DegradeRule
//        FileRefreshableDataSource<List<DegradeRule>> degradeRuleDataSource
//                = new FileRefreshableDataSource<>(
//                degradeRulePath, degradeRuleListParser);
//        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
//
//        // Data source for SystemRule
//        FileRefreshableDataSource<List<SystemRule>> systemRuleDataSource
//                = new FileRefreshableDataSource<>(
//                systemRulePath, systemRuleListParser);
//        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
    }

    private Converter<String, List<FlowRule>> flowRuleListParser = source -> JSON.parseObject(source,
            new TypeReference<List<FlowRule>>() {
            });
    private Converter<String, List<DegradeRule>> degradeRuleListParser = source -> JSON.parseObject(source,
            new TypeReference<List<DegradeRule>>() {
            });
    private Converter<String, List<SystemRule>> systemRuleListParser = source -> JSON.parseObject(source,
            new TypeReference<List<SystemRule>>() {
            });

    private String getFilePathInJar(String jarPath, String resourcePath) {
        // remove 'file:' and '!/' in jarPath
        int index = jarPath.length() + 7;
        return resourcePath.substring(index);
    }
}
