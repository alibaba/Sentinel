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
package com.alibaba.csp.sentinel.adapter.spring.restclient.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Fallback handler for RestClient when request is blocked by Sentinel.
 *
 * @author QHT, uuuyuqi
 */
public interface RestClientFallback {

    /**
     * Handle the blocked request and return a fallback response.
     *
     * @param request HTTP request entity
     * @param body request body
     * @param execution request execution
     * @param ex the block exception
     * @return fallback response
     */
    ClientHttpResponse handle(HttpRequest request, byte[] body, 
                              ClientHttpRequestExecution execution, BlockException ex);
}