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
package com.alibaba.csp.sentinel.adapter.apache.httpclient5.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;

/**
 * Fallback handler for Apache HttpClient 5.x when the request is blocked by Sentinel.
 *
 * @author uuuyuqi
 */
public interface ApacheHttpClientFallback {

    /**
     * Handle the blocked request.
     *
     * @param request the original HTTP request
     * @param e       the block exception
     * @return the fallback response
     */
    ClassicHttpResponse handle(ClassicHttpRequest request, BlockException e);
}
