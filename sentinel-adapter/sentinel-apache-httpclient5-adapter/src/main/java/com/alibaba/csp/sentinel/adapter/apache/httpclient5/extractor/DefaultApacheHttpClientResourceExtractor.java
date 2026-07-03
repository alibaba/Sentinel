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

import com.alibaba.csp.sentinel.log.RecordLog;
import org.apache.hc.core5.http.ClassicHttpRequest;

/**
 * Default implementation of {@link ApacheHttpClientResourceExtractor}.
 *
 * <p>Generates resource name in the format {@code METHOD:url}, with query string
 * and fragment stripped. This is consistent with the OkHttp adapter's resource naming
 * convention.</p>
 *
 * @author uuuyuqi
 */
public class DefaultApacheHttpClientResourceExtractor implements ApacheHttpClientResourceExtractor {

    @Override
    public String extractor(ClassicHttpRequest request) {
        try {
            String httpMethod = request.getMethod();
            String originalUrl = request.getUri().toString();
            int firstIndexOfQuery = originalUrl.indexOf('?');
            int firstIndexOfFragment = originalUrl.indexOf('#');
            if (firstIndexOfFragment < 0 && firstIndexOfQuery < 0) {
                return httpMethod + ":" + originalUrl;
            }
            if (firstIndexOfFragment > 0 && firstIndexOfQuery > 0) {
                int pos = Math.min(firstIndexOfQuery, firstIndexOfFragment);
                return httpMethod + ":" + originalUrl.substring(0, pos);
            } else if (firstIndexOfQuery > 0) {
                return httpMethod + ":" + originalUrl.substring(0, firstIndexOfQuery);
            } else {
                return httpMethod + ":" + originalUrl.substring(0, firstIndexOfFragment);
            }
        } catch (Exception ex) {
            RecordLog.warn("Failed to extract resource name of HttpClient 5 request, request={}", request, ex);
            return null;
        }
    }
}
