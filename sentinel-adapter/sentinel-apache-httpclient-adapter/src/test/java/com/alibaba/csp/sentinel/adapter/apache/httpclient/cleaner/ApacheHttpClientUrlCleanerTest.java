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
package com.alibaba.csp.sentinel.adapter.apache.httpclient.cleaner;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

/**
 * @author zhaoyuguang
 */
public class ApacheHttpClientUrlCleanerTest {

        @Test
        public void testDefaultApacheHttpClientUrlCleaner() {
            ApacheHttpClientUrlCleaner cleaner = new DefaultApacheHttpClientUrlCleaner();
            HttpGet httpGet = new HttpGet("http://localhost:8083/httpclient/back");
            cleaner.clean(httpGet, null);
            System.out.println(cleaner.clean(httpGet, null));
        }

        @Test
        public void testDefaultApacheHttpClientUrlCleaner2() {
            ApacheHttpClientUrlCleaner cleaner = new ApacheHttpClientUrlCleaner() {
                @Override
                public String clean(HttpRequest request, HttpContext context) {
                    if (request.getRequestLine().getUri().startsWith("/httpclient/back/")) {
                        return request.getRequestLine().getMethod() + ":/httpclient/back/{id}";
                    }
                    return request.getRequestLine().getMethod() + ":" + request.getRequestLine().getUri();
                }
            };
            HttpGet httpGet = new HttpGet("http://localhost:8083/httpclient/back/1");
            cleaner.clean(httpGet, null);
            System.out.println(cleaner.clean(httpGet, null));
        }
}
