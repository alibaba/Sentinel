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
package com.alibaba.csp.sentinel.datasource.spring.cloud.config.test;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.SentinelRuleLocator;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.SpringCloudConfigDataSource;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.client.ConfigClient;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.server.ConfigServer;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Before test, please start ${@link ConfigServer} and ${@link ConfigClient}
 *
 * @author lianglin
 * @since 1.7.0
 */
@RestController
@RequestMapping(value = "/test/dataSource/")
public class SpringCouldDataSourceTest {


    @Autowired
    private SentinelRuleLocator locator;

    Converter<String, List<FlowRule>> converter = new Converter<String, List<FlowRule>>() {
        @Override
        public List<FlowRule> convert(String source) {
            return JSON.parseArray(source, FlowRule.class);
        }
    };


    @GetMapping("/get")
    @ResponseBody
    public List<FlowRule> get() {
        SpringCloudConfigDataSource dataSource = new SpringCloudConfigDataSource("flow_rule", converter);
        FlowRuleManager.register2Property(dataSource.getProperty());
        return FlowRuleManager.getRules();
    }

    /**
     * WebHook refresh config
     */
    @GetMapping("/refresh")
    @ResponseBody
    public List<FlowRule> refresh() {
        locator.refresh();
        return FlowRuleManager.getRules();
    }
}
