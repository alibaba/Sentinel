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
package com.alibaba.csp.sentinel.adapter.okhttp.cleaner;

import okhttp3.Request;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author zhaoyuguang
 */

public class OkHttpUrlCleanerTest {

    @Test
    public void testDefaultOkHttpUrlCleaner() {
        OkHttpUrlCleaner cleaner = new DefaultOkHttpUrlCleaner();
        String url = "http://localhost:8083/okhttp/back";
        Request request = new Request.Builder()
                .url(url)
                .build();
        cleaner.clean(request, null);
        assertEquals(url, cleaner.clean(request, null));
    }

    @Test
    public void testCustomizeOkHttpUrlCleaner() {
        OkHttpUrlCleaner cleaner = (request, connection) -> {
            String url = request.url().toString();
            String regex = "/okhttp/back/";
            if (url.contains(regex)) {
                url = url.substring(0, url.indexOf(regex) + regex.length()) + "{id}";
            }
            return url;
        };
        String url = "http://localhost:8083/okhttp/back/abc";
        Request request = new Request.Builder()
                .url(url)
                .build();
        cleaner.clean(request, null);
        assertEquals("http://localhost:8083/okhttp/back/{id}", cleaner.clean(request, null));
    }
}