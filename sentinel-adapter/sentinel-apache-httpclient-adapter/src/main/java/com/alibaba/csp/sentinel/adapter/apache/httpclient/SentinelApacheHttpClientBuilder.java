/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.apache.httpclient;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * @author zhaoyuguang
 */
public class SentinelApacheHttpClientBuilder {

    public static HttpClientBuilder httpClientBuilder() {
        return HttpClients.custom()
                .addInterceptorFirst(new SentinelApacheHttpRequestInterceptor())
                .addInterceptorFirst(new SentinelApacheHttpResponseInterceptor());
    }

    public static HttpAsyncClientBuilder httpAsyncClientBuilder() {
        return HttpAsyncClients.custom()
                .addInterceptorFirst(new SentinelApacheHttpRequestInterceptor())
                .addInterceptorFirst(new SentinelApacheHttpResponseInterceptor());
    }
}
