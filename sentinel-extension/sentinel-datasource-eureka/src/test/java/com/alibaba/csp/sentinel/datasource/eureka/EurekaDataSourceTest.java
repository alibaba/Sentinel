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


package com.alibaba.csp.sentinel.datasource.eureka;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * @author liyang
 */
@RunWith(SpringRunner.class)
@EnableEurekaServer
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EurekaDataSourceTest {

    private static final String SENTINEL_KEY = "sentinel-rules";

    @Value("${server.port}")
    private int port;

    @Value("${eureka.instance.appname}")
    private String appname;

    @Value("${eureka.instance.instance-id}")
    private String instanceId;


    @Test
    public void testEurekaDataSource() throws Exception {
        String url = "http://localhost:" + port + "/eureka";

        EurekaDataSource<List<FlowRule>> eurekaDataSource = new EurekaDataSource(appname, instanceId, Arrays.asList(url)
                , SENTINEL_KEY, new Converter<String, List<FlowRule>>() {
            @Override
            public List<FlowRule> convert(String source) {
                return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                });
            }
        });
        FlowRuleManager.register2Property(eurekaDataSource.getProperty());

        await().timeout(15, TimeUnit.SECONDS)
                .until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return FlowRuleManager.getRules().size() > 0;
                    }
                });
        Assert.assertTrue(FlowRuleManager.getRules().size() > 0);
    }


}
