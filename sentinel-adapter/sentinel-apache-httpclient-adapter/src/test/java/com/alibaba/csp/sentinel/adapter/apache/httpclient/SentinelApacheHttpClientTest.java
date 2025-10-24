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
package com.alibaba.csp.sentinel.adapter.apache.httpclient;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.app.TestApplication;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.config.SentinelApacheHttpClientConfig;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.extractor.ApacheHttpClientResourceExtractor;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveDegradeRule;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author zhaoyuguang
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8184"
        })
public class SentinelApacheHttpClientTest {

    @Value("${server.port}")
    private Integer port;

    @Test
    public void testSentinelOkHttpInterceptor0() throws Exception {

        CloseableHttpClient httpclient = new SentinelApacheHttpClientBuilder().build();

        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/httpclient/back");
        System.out.println(getRemoteString(httpclient, httpGet));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode("httpclient:/httpclient/back");
        assertNotNull(cn);
        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }

    @Test
    public void testSentinelOkHttpInterceptor1() throws Exception {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setExtractor(new ApacheHttpClientResourceExtractor() {

            @Override
            public String extractor(HttpRequestWrapper request) {
                String contains = "/httpclient/back/";
                String uri = request.getRequestLine().getUri();
                if (uri.startsWith(contains)) {
                    uri = uri.substring(0, uri.indexOf(contains) + contains.length()) + "{id}";
                }
                return request.getMethod() + ":" + uri;
            }
        });
        CloseableHttpClient httpclient = new SentinelApacheHttpClientBuilder(config).build();

        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/httpclient/back/1");
        System.out.println(getRemoteString(httpclient, httpGet));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode("httpclient:GET:/httpclient/back/{id}");
        assertNotNull(cn);
        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }

    @Test
    public void testAdaptiveDegradeHeaderAndMetricsRemoval() throws Exception {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setExtractor(new ApacheHttpClientResourceExtractor() {
            @Override
            public String extractor(HttpRequestWrapper request) {
                String uri = request.getRequestLine().getUri();
                if (uri.equals("/httpclient/back/adaptive")) {
                    return request.getMethod() + ":" + uri;
                }
                return request.getMethod() + ":" + uri;
            }
        });
        String resourceName = "httpclient:GET:/httpclient/back/adaptive";
        AdaptiveDegradeRule adaptiveDegradeRule = new AdaptiveDegradeRule(resourceName);
        adaptiveDegradeRule.setEnabled(true);
        try {
            CloseableHttpClient httpclient = new SentinelApacheHttpClientBuilder(config).build();
            HttpGet httpGet = new HttpGet("http://localhost:" + port + "/httpclient/back/adaptive");
            HttpResponse resp = executeWithHeaders(httpclient, httpGet);
            assertEquals("adaptive-enabled-received", resp.body);
            Header metricsHeader = resp.getFirstHeader();
            assertNull("Sentinel should remove X-Server-Metrics from response", metricsHeader);
        } finally {
            Constants.ROOT.removeChildList();
            ClusterBuilderSlot.getClusterNodeMap().clear();
        }
    }

    private String getRemoteString(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {
        String result;
        HttpContext context = new BasicHttpContext();
        CloseableHttpResponse response;
        response = httpclient.execute(httpGet, context);
        try {
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        httpclient.close();
        return result;
    }

    private static class HttpResponse {
        final String body;
        final Header[] headers;

        HttpResponse(String body, Header[] headers) {
            this.body = body;
            this.headers = headers;
        }

        Header getFirstHeader() {
            for (Header h : headers) {
                if ("X-Server-Metrics".equalsIgnoreCase(h.getName())) {
                    return h;
                }
            }
            return null;
        }
    }

    private HttpResponse executeWithHeaders(CloseableHttpClient client, HttpGet request) throws IOException {
        HttpContext context = new BasicHttpContext();
        CloseableHttpResponse response = client.execute(request, context);
        try {
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity, "UTF-8");
            Header[] headers = response.getAllHeaders();
            EntityUtils.consume(entity);
            return new HttpResponse(body, headers);
        } finally {
            response.close();
            client.close();
        }
    }
}
