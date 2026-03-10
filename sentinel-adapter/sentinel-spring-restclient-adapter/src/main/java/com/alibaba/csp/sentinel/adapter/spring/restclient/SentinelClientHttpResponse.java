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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Default HTTP response when request is blocked by Sentinel.
 *
 * @author QHT, uuuyuqi
 */
public class SentinelClientHttpResponse implements ClientHttpResponse {

    private String blockResponse = "Request blocked by Sentinel";

    public SentinelClientHttpResponse() {
    }

    public SentinelClientHttpResponse(String blockResponse) {
        this.blockResponse = blockResponse;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.OK;
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return HttpStatus.OK.value();
    }

    @Override
    public String getStatusText() throws IOException {
        return blockResponse;
    }

    @Override
    public void close() {
    }

    @Override
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(blockResponse.getBytes());
    }

    @Override
    public HttpHeaders getHeaders() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE,
                Arrays.asList(MediaType.APPLICATION_JSON_VALUE));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.putAll(headers);
        return httpHeaders;
    }
}