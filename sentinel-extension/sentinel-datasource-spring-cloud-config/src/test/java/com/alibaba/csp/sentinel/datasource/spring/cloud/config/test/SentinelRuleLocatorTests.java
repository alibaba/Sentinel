/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel.datasource.spring.cloud.config.test;

import com.alibaba.csp.sentinel.datasource.spring.cloud.config.SentinelRuleLocator;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.SentinelRuleStorage;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.config.DataSourceBootstrapConfiguration;
import com.alibaba.csp.sentinel.datasource.spring.cloud.config.server.ConfigServer;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author lianglin
 * @since 1.7.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataSourceBootstrapConfiguration.class, properties = {
        "spring.application.name=sentinel"
})
public class SentinelRuleLocatorTests {


    @Autowired
    private SentinelRuleLocator sentinelRulesSourceLocator;

    @Autowired
    private Environment environment;

    @Test
    public void testAutoLoad() {
        Assert.assertTrue(sentinelRulesSourceLocator != null);
        Assert.assertTrue(environment != null);
    }


    /**
     * Before run this test case, please start the Config Server ${@link ConfigServer}
     */
    public void testLocate() {
        ConfigClientProperties configClientProperties = new ConfigClientProperties(environment);
        configClientProperties.setLabel("master");
        configClientProperties.setProfile("dev");
        configClientProperties.setUri(new String[]{"http://localhost:10086/"});
        SentinelRuleLocator sentinelRulesSourceLocator = new SentinelRuleLocator(configClientProperties, environment);
        sentinelRulesSourceLocator.locate(environment);
        Assert.assertTrue(StringUtil.isNotBlank(SentinelRuleStorage.retrieveRule("flow_rule")));

    }

}
