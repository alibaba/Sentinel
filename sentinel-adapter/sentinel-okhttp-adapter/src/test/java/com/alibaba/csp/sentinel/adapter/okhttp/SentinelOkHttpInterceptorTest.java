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
import com.alibaba.csp.sentinel.adapter.okhttp.extractor.OkHttpResourceExtractor;
import com.alibaba.csp.sentinel.adapter.okhttp.fallback.DefaultOkHttpFallback;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveDegradeRule;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import okhttp3.Connection;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
        // With prefix
        SentinelOkHttpConfig config = new SentinelOkHttpConfig("okhttp:");
        String url0 = "http://localhost:" + port + "/okhttp/back";
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SentinelOkHttpInterceptor(config))
                .build();
        Request request = new Request.Builder()
                .url(url0)
                .build();
        System.out.println(client.newCall(request).execute().body().string());
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(config.getResourcePrefix() + "GET:" + url0);
        assertNotNull(cn);

        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }

    @Test
    public void testSentinelOkHttpInterceptor1() throws Exception {

        String url0 = "http://localhost:" + port + "/okhttp/back/1";
        SentinelOkHttpConfig config = new SentinelOkHttpConfig(new OkHttpResourceExtractor() {
            @Override
            public String extract(Request request, Connection connection) {
                String regex = "/okhttp/back/";
                String url = request.url().toString();
                if (url.contains(regex)) {
                    url = url.substring(0, url.indexOf(regex) + regex.length()) + "{id}";
                }
                return request.method() + ":" + url;
            }
        }, new DefaultOkHttpFallback());
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SentinelOkHttpInterceptor(config))
                .build();
        Request request = new Request.Builder()
                .url(url0)
                .build();
        System.out.println(client.newCall(request).execute().body().string());

        String url1 = config.getResourcePrefix() + "GET:http://localhost:" + port + "/okhttp/back/{id}";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url1);
        assertNotNull(cn);

        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }

    @Test
    public void testAdaptiveDegradeHeaderAndMetricsRemoval() throws Exception {
        String url = "http://localhost:" + port + "/okhttp/back/adaptive";
        SentinelOkHttpConfig config = new SentinelOkHttpConfig(new OkHttpResourceExtractor() {
            @Override
            public String extract(Request request, Connection connection) {
                String regex = "/okhttp/back/";
                String url = request.url().toString();
                if (url.contains(regex)) {
                    url = url.substring(0, url.indexOf(regex) + regex.length()) + "{id}";
                }
                return request.method() + ":" + url;
            }
        }, new DefaultOkHttpFallback());
        String url1 = config.getResourcePrefix() + "GET:http://localhost:" + port + "/okhttp/back/{id}";
        AdaptiveDegradeRule adaptiveDegradeRule = new AdaptiveDegradeRule(url1);
        adaptiveDegradeRule.setEnabled(true);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SentinelOkHttpInterceptor(config))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        org.junit.Assert.assertTrue(
                "Response should indicate adaptive header was received",
                responseBody.contains("Adaptive enabled received")
        );
        org.junit.Assert.assertNull(
                "X-Server-Metrics header should be removed from client response",
                response.header("X-Server-Metrics")
        );
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url1);
        org.junit.Assert.assertNotNull("ClusterNode should exist for resource", cn);
        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }
}
