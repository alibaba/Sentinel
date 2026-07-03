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
package com.alibaba.csp.sentinel.dashboard.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.RequestContent;
import org.junit.Test;

public class SentinelApiClientTest {
    @Test
    public void postRequest() throws HttpException, IOException {
        // Processor is required because it will determine the final request body including
        // headers before outgoing.
        RequestContent processor = new RequestContent();
        Map<String, String> params = new HashMap<String, String>();
        params.put("a", "1");
        params.put("b", "2+");
        params.put("c", "3 ");
        
        HttpUriRequest request;
        
        request = SentinelApiClient.postRequest("/test", params, false);
        assertNotNull(request);
        processor.process(request, null);
        assertNotNull(request.getFirstHeader("Content-Type"));
        assertEquals("application/x-www-form-urlencoded", request.getFirstHeader("Content-Type").getValue());
        
        request = SentinelApiClient.postRequest("/test", params, true);
        assertNotNull(request);
        processor.process(request, null);
        assertNotNull(request.getFirstHeader("Content-Type"));
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", request.getFirstHeader("Content-Type").getValue());
    }
}
