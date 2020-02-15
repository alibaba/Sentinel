/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dubbo.springboot.configuration;

import com.alibaba.csp.sentinel.dubbo.springboot.api.impl.TestClient;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import org.apache.dubbo.spring.boot.autoconfigure.DubboAutoConfiguration;
import org.apache.dubbo.spring.boot.autoconfigure.DubboRelaxedBinding2AutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DubboRelaxedBinding2AutoConfiguration.class
        , SentinelServiceBeanPostProcessorTest.class
        , DubboAutoConfiguration.class
        , SentinelAutoConfiguration.class},
        properties = {"dubbo.scan.base-packages=com.alibaba.csp.sentinel.dubbo.springboot.api.impl", "flow.count=10"})
@PropertySource(value = "classpath:/dubbo.properties")
@Configuration
public class SentinelServiceBeanPostProcessorTest {


    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Bean
    public TestClient testClient() {
        return new TestClient();
    }


    @Test
    public void test() {
        assertNotNull(beanFactory);
        SentinelServiceBeanPostProcessor sentinelServiceBeanPostProcessor = beanFactory.getBean(SentinelServiceBeanPostProcessor.class);
        assertNotNull(sentinelServiceBeanPostProcessor);
        assertEquals(2, sentinelServiceBeanPostProcessor.flowRules.size());
        assertEquals(1, sentinelServiceBeanPostProcessor.degradeRules.size());

        FlowRule flowRule = sentinelServiceBeanPostProcessor.flowRules.get(0);
        assertEquals(10, flowRule.getCount(), 0);
        assertEquals(1, sentinelServiceBeanPostProcessor.providerFallbackManager.getFallbackMethod().size());
        assertEquals(1, sentinelServiceBeanPostProcessor.consumerFallbackManager.getFallbackMethod().size());
    }
}