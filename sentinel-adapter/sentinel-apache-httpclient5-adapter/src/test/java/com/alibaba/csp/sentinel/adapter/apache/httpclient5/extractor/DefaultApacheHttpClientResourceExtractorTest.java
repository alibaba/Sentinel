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
package com.alibaba.csp.sentinel.adapter.apache.httpclient5.extractor;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author uuuyuqi
 */
public class DefaultApacheHttpClientResourceExtractorTest {

    private final DefaultApacheHttpClientResourceExtractor extractor =
            new DefaultApacheHttpClientResourceExtractor();

    @Test
    public void testSimpleUrl() {
        HttpGet request = new HttpGet("http://localhost:8080/api/users");
        assertEquals("GET:http://localhost:8080/api/users", extractor.extractor(request));
    }

    @Test
    public void testUrlWithQueryString() {
        HttpGet request = new HttpGet("http://localhost:8080/api/users?page=1&size=10");
        assertEquals("GET:http://localhost:8080/api/users", extractor.extractor(request));
    }

    @Test
    public void testUrlWithFragment() {
        HttpGet request = new HttpGet("http://localhost:8080/api/users#section");
        assertEquals("GET:http://localhost:8080/api/users", extractor.extractor(request));
    }

    @Test
    public void testUrlWithQueryAndFragment() {
        HttpGet request = new HttpGet("http://localhost:8080/api/users?page=1#section");
        assertEquals("GET:http://localhost:8080/api/users", extractor.extractor(request));
    }

    @Test
    public void testUrlWithFragmentBeforeQuery() {
        HttpGet request = new HttpGet("http://localhost:8080/api/users#section?page=1");
        assertEquals("GET:http://localhost:8080/api/users", extractor.extractor(request));
    }

    @Test
    public void testPostMethod() {
        HttpPost request = new HttpPost("http://localhost:8080/api/users");
        assertEquals("POST:http://localhost:8080/api/users", extractor.extractor(request));
    }

    @Test
    public void testRootPath() {
        HttpGet request = new HttpGet("http://localhost:8080/");
        assertEquals("GET:http://localhost:8080/", extractor.extractor(request));
    }

    @Test
    public void testRelativePath() {
        HttpGet request = new HttpGet("/api/users");
        assertEquals("GET:/api/users", extractor.extractor(request));
    }

    @Test
    public void testRelativePathWithQuery() {
        HttpGet request = new HttpGet("/api/users?page=1");
        assertEquals("GET:/api/users", extractor.extractor(request));
    }
}
