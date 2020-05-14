/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.okhttp;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.adapter.okhttp.app.TestApplication;
import com.alibaba.csp.sentinel.adapter.okhttp.config.SentinelOkHttpConfig;
import com.alibaba.csp.sentinel.adapter.okhttp.extractor.OkHttpResourceExtractor;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import okhttp3.Connection;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author zhaoyuguang
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8086"
        })
public class SentinelOkHttpInterceptorTest {

    @Value("${server.port}")
    private Integer port;

    @Test
    public void testSentinelOkHttpInterceptor0() throws Exception {

        String url0 = "http://localhost:" + port + "/okhttp/back";
        SentinelOkHttpConfig.setPrefix("okhttp:");
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SentinelOkHttpInterceptor())
                .build();
        Request request = new Request.Builder()
                .url(url0)
                .build();
        System.out.println(client.newCall(request).execute().body().string());
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(SentinelOkHttpConfig.getPrefix() + "GET:" + url0);
        assertNotNull(cn);

        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }

    @Test
    public void testSentinelOkHttpInterceptor1() throws Exception {

        String url0 = "http://localhost:" + port + "/okhttp/back/1";
        SentinelOkHttpConfig.setExtractor(new OkHttpResourceExtractor() {
            @Override
            public String extract(String url, Request request, Connection connection) {
                String regex = "/okhttp/back/";
                if (url.contains(regex)) {
                    url = url.substring(0, url.indexOf(regex) + regex.length()) + "{id}";
                }
                return request.method() + ":" + url;
            }
        });
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SentinelOkHttpInterceptor())
                .build();
        Request request = new Request.Builder()
                .url(url0)
                .build();
        System.out.println(client.newCall(request).execute().body().string());

        String url1 = SentinelOkHttpConfig.getPrefix() + "GET:http://localhost:" + port + "/okhttp/back/{id}";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url1);
        assertNotNull(cn);

        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }
}
