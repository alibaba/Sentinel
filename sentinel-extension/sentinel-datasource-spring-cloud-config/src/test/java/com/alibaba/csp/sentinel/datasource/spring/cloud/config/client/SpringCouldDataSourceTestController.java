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
package com.alibaba.csp.sentinel.datasource.spring.cloud.config.client;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.SentinelRuleLocator;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.SpringCloudConfigDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lianglin
 * @since 1.7.0
 */
@RestController
public class SpringCouldDataSourceTestController {

    private Logger log = LoggerFactory.getLogger(getClass());


    @Autowired
    private SentinelRuleLocator locator;

    Converter<String, List<FlowRule>> converter = new Converter<String, List<FlowRule>>() {
        @Override
        public List<FlowRule> convert(String source) {
            return JSON.parseArray(source, FlowRule.class);
        }
    };


    @GetMapping("/index")
    public String index() {
        SpringCloudConfigDataSource<String> dataSource = new SpringCloudConfigDataSource("flow_rule", converter);
        String s = dataSource.readSource();
        return s;
    }

    /**
     * WebHook refresh config
     */
    @PostMapping
    public void refresh() {
        locator.refresh();
    }
}
