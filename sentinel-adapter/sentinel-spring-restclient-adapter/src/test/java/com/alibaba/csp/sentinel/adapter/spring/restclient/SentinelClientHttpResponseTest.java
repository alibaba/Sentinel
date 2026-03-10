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
package com.alibaba.csp.sentinel.adapter.spring.restclient;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SentinelClientHttpResponse}.
 *
 * @author uuuyuqi
 */
public class SentinelClientHttpResponseTest {

    @Test
    public void testDefaultResponse() throws IOException {
        SentinelClientHttpResponse response = new SentinelClientHttpResponse();
        assertEquals("Request blocked by Sentinel", response.getStatusText());
    }

    @Test
    public void testCustomResponse() throws IOException {
        String customMessage = "Custom blocked message";
        SentinelClientHttpResponse response = new SentinelClientHttpResponse(customMessage);
        assertEquals(customMessage, response.getStatusText());
    }

    @Test
    public void testResponseProperties() throws IOException {
        SentinelClientHttpResponse response = new SentinelClientHttpResponse("test");
        
        assertNotNull(response.getStatusCode());
        assertEquals(200, response.getRawStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getHeaders());
        assertTrue(response.getHeaders().containsKey("Content-Type"));
    }
}