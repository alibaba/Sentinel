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

package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.netflix.zuul.context.RequestContext;
import org.junit.Assert;
import org.junit.Test;

import static com.alibaba.csp.sentinel.adapter.zuul.constants.ZuulConstant.ERROR_TYPE;

/**
 * @author tiger
 */
public class SentinelErrorFilterTest {
    @Test
    public void testFilterType() throws Exception {
        SentinelZuulProperties properties = new SentinelZuulProperties();
        SentinelErrorFilter sentinelErrorFilter = new SentinelErrorFilter(properties);
        Assert.assertEquals(sentinelErrorFilter.filterType(), ERROR_TYPE);
    }

    @Test
    public void testShouldFilter() {
        SentinelZuulProperties properties = new SentinelZuulProperties();
        Assert.assertFalse(properties.isEnabled());
        properties.setEnabled(true);
        SentinelErrorFilter sentinelErrorFilter = new SentinelErrorFilter(properties);
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setThrowable(new RuntimeException());
        Assert.assertTrue(sentinelErrorFilter.shouldFilter());
    }

    @Test
    public void testRun() throws Exception {
        SentinelZuulProperties properties = new SentinelZuulProperties();
        SentinelErrorFilter sentinelErrorFilter = new SentinelErrorFilter(properties);
        Object result = sentinelErrorFilter.run();
        Assert.assertNull(result);
    }

}