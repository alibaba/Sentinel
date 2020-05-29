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
package com.alibaba.csp.sentinel.adapter.apache.httpclient.extractor;

import org.apache.http.client.methods.HttpRequestWrapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author zhaoyuguang
 */
public class ApacheHttpClientResourceExtractorTest {

    @Test
    public void testDefaultOkHttpResourceExtractor() {
        ApacheHttpClientResourceExtractor extractor = new ApacheHttpClientResourceExtractor() {
            @Override
            public String extractor(String method, String uri, HttpRequestWrapper request) {
                String regex = "/httpclient/back/";
                if (uri.contains(regex)) {
                    uri = uri.substring(0, uri.indexOf(regex) + regex.length()) + "{id}";
                }
                return method + ":" + uri;
            }
        };
        System.out.println(extractor.extractor("GET", "/httpclient/back/1", null));
        assertEquals("GET:/httpclient/back/{id}", extractor.extractor("GET", "/httpclient/back/1", null));
    }
}