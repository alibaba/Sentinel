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

import org.apache.hc.core5.http.ClassicHttpRequest;

/**
 * Extracts Sentinel resource name from an Apache HttpClient 5.x request.
 *
 * @author uuuyuqi
 */
public interface ApacheHttpClientResourceExtractor {

    /**
     * Extract resource name from the given request.
     *
     * @param request the HTTP request
     * @return the resource name, or {@code null}/{@code ""} to skip Sentinel protection
     */
    String extractor(ClassicHttpRequest request);
}
