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

import com.alibaba.csp.sentinel.datasource.*;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * If your app run with jar, it can work
 * A sample showing how to register readable and writable data source via Sentinel init SPI mechanism.
 * </p>
 * <p>
 * To activate this, you can add the class name to `com.alibaba.csp.sentinel.init.InitFunc` file
 * in `META-INF/services/` directory of the resource directory. Then the data source will be automatically
 * registered during the initialization of Sentinel.
 * </p>
 *
 * @author dingq
 */
public class JarFileDataSourceInit implements InitFunc {

    @Override
    public void init() throws Exception {
        String flowRuleJarName = System.getProperty("user.dir") + "/sentinel-demo/sentinel-demo-dynamic-file-rule/target/sentinel-demo-dynamic-file-rule-1.5.1-SNAPSHOT.jar";
        // eg: if flowRuleInJarName full path is 'sentinel-demo-dynamic-file-rule-1.5.1-SNAPSHOT.jar!/classes/FlowRule.json',
        // your flowRuleInJarName is 'classes/FlowRule.json'
        String flowRuleInJarName = "FlowRule.json";

        ReadableDataSource<String, List<FlowRule>> ds = new JarFileRefreshableDataSource<>(
                flowRuleJarName, flowRuleInJarName, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
        }));
        // Register to flow rule manager.
        FlowRuleManager.register2Property(ds.getProperty());

        WritableDataSource<List<FlowRule>> wds = new JarFileWritableDataSource<>(flowRuleJarName, flowRuleInJarName, this::encodeJson);
        // Register to writable data source registry so that rules can be updated to file
        // when there are rules pushed from the Sentinel Dashboard.
        WritableDataSourceRegistry.registerFlowDataSource(wds);
    }

    private <T> String encodeJson(T t) {
        return JSON.toJSONString(t);
    }
    public static void main(String[] args) throws Exception{
        // A fake path.
        String flowRuleJarPath = System.getProperty("user.dir")+"/sentinel-demo/sentinel-demo-dynamic-file-rule/target/sentinel-demo-dynamic-file-rule-1.5.1-SNAPSHOT.jar";
        String flowRuleInJarPath = "FlowRule.json";

        JarFileDataSourceInit jarFileDataSourceInit = new JarFileDataSourceInit();
        WritableDataSource<List<FlowRule>> wds = new JarFileWritableDataSource<>(flowRuleJarPath,flowRuleInJarPath, (t) ->JSON.toJSONString(t));
        List<FlowRule> list = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setCount(10);
        flowRule.setStrategy(1);
        flowRule.setRefResource("test");
        list.add(flowRule);
        wds.write(list);
    }


}
