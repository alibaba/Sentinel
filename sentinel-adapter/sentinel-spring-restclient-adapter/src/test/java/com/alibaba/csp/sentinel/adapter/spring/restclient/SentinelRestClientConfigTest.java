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

import com.alibaba.csp.sentinel.adapter.spring.restclient.extractor.RestClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.fallback.DefaultRestClientFallback;
import org.junit.Test;

/**
 * Tests for {@link SentinelRestClientConfig}.
 *
 * @author uuuyuqi
 */
public class SentinelRestClientConfigTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConfigSetExtractorNull() {
        new SentinelRestClientConfig(null, new DefaultRestClientFallback());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigSetFallbackNull() {
        new SentinelRestClientConfig(new RestClientResourceExtractor() {
            @Override
            public String extract(org.springframework.http.HttpRequest request) {
                return request.getURI().toString();
            }
        }, null);
    }
}