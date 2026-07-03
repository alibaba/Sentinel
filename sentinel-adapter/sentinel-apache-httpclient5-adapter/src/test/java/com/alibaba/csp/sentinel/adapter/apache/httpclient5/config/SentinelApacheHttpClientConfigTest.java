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
package com.alibaba.csp.sentinel.adapter.apache.httpclient5.config;

import com.alibaba.csp.sentinel.adapter.apache.httpclient5.extractor.DefaultApacheHttpClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.fallback.DefaultApacheHttpClientFallback;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author uuuyuqi
 */
public class SentinelApacheHttpClientConfigTest {

    @Test
    public void testDefaultConfig() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        assertEquals("httpclient:", config.getPrefix());
        assertNotNull(config.getExtractor());
        assertNotNull(config.getFallback());
        assertTrue(config.getExtractor() instanceof DefaultApacheHttpClientResourceExtractor);
        assertTrue(config.getFallback() instanceof DefaultApacheHttpClientFallback);
    }

    @Test
    public void testSetPrefix() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setPrefix("custom:");
        assertEquals("custom:", config.getPrefix());
    }

    @Test
    public void testSetEmptyPrefix() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setPrefix("");
        assertEquals("", config.getPrefix());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullPrefix() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setPrefix(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullExtractor() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setExtractor(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullFallback() {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setFallback(null);
    }
}
