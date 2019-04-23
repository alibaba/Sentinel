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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.filters;

import org.junit.Assert;
import org.junit.Test;

import static com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant.POST_TYPE;

/**
 * @author tiger
 */
public class SentinelZuulPostFilterTest {

    @Test
    public void testFilterType() throws Exception {
        SentinelZuulPostFilter sentinelZuulPostFilter = new SentinelZuulPostFilter();
        Assert.assertEquals(sentinelZuulPostFilter.filterType(), POST_TYPE);
    }

    @Test
    public void testRun() throws Exception {
        SentinelZuulPostFilter sentinelZuulPostFilter = new SentinelZuulPostFilter();
        Object result = sentinelZuulPostFilter.run();
        Assert.assertNull(result);
    }

}