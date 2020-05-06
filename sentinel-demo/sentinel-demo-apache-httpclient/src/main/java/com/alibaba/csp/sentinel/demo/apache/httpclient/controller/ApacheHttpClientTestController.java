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
package com.alibaba.csp.sentinel.demo.apache.httpclient.controller;

import com.alibaba.csp.sentinel.adapter.apache.httpclient.SentinelApacheHttpClientBuilder;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.config.SentinelApacheHttpClientConfig;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.exception.SentinelApacheHttpClientHandleException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
public class ApacheHttpClientTestController {

    @Value("${server.port}")
    private Integer port;

    @RequestMapping("/httpclient/back")
    public String back() {
        System.out.println("back");
        return "Welcome Back!";
    }

    @RequestMapping("/httpclient/back/{id}")
    public String back(@PathVariable String id) {
        System.out.println("back");
        return "Welcome Back! " + id;
    }

    @RequestMapping("/httpclient/sync")
    public String sync() throws Exception {
        CloseableHttpClient httpclient = SentinelApacheHttpClientBuilder.httpClientBuilder().build();

        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/httpclient/back");
        return getRemoteString(httpclient, httpGet);
    }

    @RequestMapping("/httpclient/sync/{id}")
    public String sync(@PathVariable String id) throws Exception {
        CloseableHttpClient httpclient = SentinelApacheHttpClientBuilder.httpClientBuilder().build();

        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/httpclient/back/" + id);
        return getRemoteString(httpclient, httpGet);
    }

    private String getRemoteString(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {
        String result;
        HttpContext context = new BasicHttpContext();
        CloseableHttpResponse response;
        try {
            response = httpclient.execute(httpGet, context);
        } catch (Exception e){
            SentinelApacheHttpClientHandleException.handle(context, e);
            throw e;
        }
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

    @RequestMapping("/httpclient/async")
    public String async() throws Exception {
        CloseableHttpAsyncClient httpclient = SentinelApacheHttpClientBuilder.httpAsyncClientBuilder().build();
        httpclient.start();

        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/httpclient/back");
        Future<HttpResponse> future = httpclient.execute(httpGet, null);
        HttpResponse response = future.get(100L, TimeUnit.MILLISECONDS);
        httpclient.close();
        return EntityUtils.toString(response.getEntity(), "utf-8");
    }

}
