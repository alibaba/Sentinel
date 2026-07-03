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
package com.alibaba.csp.sentinel.adapter.apache.httpclient5;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.app.TestApplication;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.config.SentinelApacheHttpClientConfig;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.extractor.ApacheHttpClientResourceExtractor;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author uuuyuqi
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
        "server.port=8185"
    })
public class SentinelApacheHttpClientTest {

    @Value("${server.port}")
    private Integer port;

    @Test
    public void testDefaultInterceptor() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom()
                .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
                    new SentinelApacheHttpClient5Handler())
                .build();

        String url = "http://localhost:" + port + "/httpclient/back";
        HttpGet httpGet = new HttpGet(url);
        getRemoteString(httpclient, httpGet);
        ClusterNode cn = ClusterBuilderSlot.getClusterNode("httpclient:GET:" + url);
        assertNotNull(cn);
    }

    @Test
    public void testWithUrlQuery() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom()
                .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
                    new SentinelApacheHttpClient5Handler())
                .build();

        String urlWithQuery = "http://localhost:" + port + "/httpclient/query?foo=baz&baz=foo";
        String urlWithoutQuery = "http://localhost:" + port + "/httpclient/query";
        HttpGet httpGet = new HttpGet(urlWithQuery);
        getRemoteString(httpclient, httpGet);
        assertNotNull(ClusterBuilderSlot.getClusterNode("httpclient:GET:" + urlWithoutQuery));
        assertNull(ClusterBuilderSlot.getClusterNode("httpclient:GET:" + urlWithQuery));
    }

    @Test
    public void testWithUrlQueryAndFragment() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom()
                .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
                    new SentinelApacheHttpClient5Handler())
                .build();

        String urlPrefix = "http://localhost:" + port + "/httpclient/fragment";
        String suffix = "#foo?baz=xxx";
        HttpGet httpGet = new HttpGet(urlPrefix + suffix);
        getRemoteString(httpclient, httpGet);
        assertNotNull(ClusterBuilderSlot.getClusterNode("httpclient:GET:" + urlPrefix));
        assertNull(ClusterBuilderSlot.getClusterNode("httpclient:GET:" + urlPrefix + suffix));
    }

    @Test
    public void testWithCustomizedResourceExtractor() throws Exception {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setExtractor(new ApacheHttpClientResourceExtractor() {
            @Override
            public String extractor(ClassicHttpRequest request) {
                String contains = "/httpclient/back/";
                String uri;
                try {
                    uri = request.getUri().toString();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                if (uri.contains(contains)) {
                    uri = uri.substring(0, uri.indexOf(contains) + contains.length()) + "{id}";
                }
                return request.getMethod() + ":" + uri;
            }
        });
        CloseableHttpClient httpclient = HttpClients.custom()
                .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
                    new SentinelApacheHttpClient5Handler(config))
                .build();

        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/httpclient/back/1");
        getRemoteString(httpclient, httpGet);
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(
            "httpclient:GET:http://localhost:" + port + "/httpclient/back/{id}");
        assertNotNull(cn);
    }

    @Test
    public void testWithEmptyPrefix() throws Exception {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setPrefix("");

        CloseableHttpClient httpclient = HttpClients.custom()
                .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
                    new SentinelApacheHttpClient5Handler(config))
                .build();

        String url = "http://localhost:" + port + "/httpclient/noprefix";
        HttpGet httpGet = new HttpGet(url);
        getRemoteString(httpclient, httpGet);
        ClusterNode cn = ClusterBuilderSlot.getClusterNode("GET:" + url);
        assertNotNull(cn);
    }

    private String getRemoteString(CloseableHttpClient httpclient, HttpGet httpGet)
            throws IOException, ParseException {
        String result;
        HttpContext context = new BasicHttpContext();
        CloseableHttpResponse response = httpclient.execute(httpGet, context);
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
}
