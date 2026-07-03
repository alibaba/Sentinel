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
package com.alibaba.csp.sentinel.adapter.apache.httpclient.config;

import org.junit.Test;

/**
 * @author zhaoyuguang
 */
public class SentinelApacheHttpClientConfigTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConfigSetPrefix() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setPrefix(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigSetCleaner() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setExtractor(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigSetFallback() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setFallback(null);
    }
}
