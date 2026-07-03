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

import java.io.File;
import java.util.List;

import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.datasource.FileWritableDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * <p>
 * A sample showing how to register readable and writable data source via Sentinel init SPI mechanism.
 * </p>
 * <p>
 * To activate this, you can add the class name to `com.alibaba.csp.sentinel.init.InitFunc` file
 * in `META-INF/services/` directory of the resource directory. Then the data source will be automatically
 * registered during the initialization of Sentinel.
 * </p>
 *
 * @author Eric Zhao
 */
public class FileDataSourceInit implements InitFunc {

    @Override
    public void init() throws Exception {
        // A fake path.
        String flowRuleDir = System.getProperty("user.home") + File.separator + "sentinel" + File.separator + "rules";
        String flowRuleFile = "flowRule.json";
        String flowRulePath = flowRuleDir + File.separator + flowRuleFile;

        ReadableDataSource<String, List<FlowRule>> ds = new FileRefreshableDataSource<>(
            flowRulePath, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {})
        );
        // Register to flow rule manager.
        FlowRuleManager.register2Property(ds.getProperty());

        WritableDataSource<List<FlowRule>> wds = new FileWritableDataSource<>(flowRulePath, this::encodeJson);
        // Register to writable data source registry so that rules can be updated to file
        // when there are rules pushed from the Sentinel Dashboard.
        WritableDataSourceRegistry.registerFlowDataSource(wds);
    }

    private <T> String encodeJson(T t) {
        return JSON.toJSONString(t);
    }
}
