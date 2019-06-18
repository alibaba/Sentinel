/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.param;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class GatewayRegexCacheTest {

    @Before
    public void setUp() {
        GatewayRegexCache.clear();
    }

    @After
    public void tearDown() {
        GatewayRegexCache.clear();
    }

    @Test
    public void testAddAndGetRegexPattern() {
        // Test for invalid pattern.
        assertThat(GatewayRegexCache.addRegexPattern("\\")).isFalse();
        assertThat(GatewayRegexCache.addRegexPattern(null)).isFalse();
        // Test for good pattern.
        String goodPattern = "\\d+";
        assertThat(GatewayRegexCache.addRegexPattern(goodPattern)).isTrue();
        assertThat(GatewayRegexCache.getRegexPattern(goodPattern)).isNotNull();
    }
}
