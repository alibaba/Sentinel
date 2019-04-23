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

import com.netflix.zuul.context.RequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant.PRE_TYPE;
import static com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant.SERVICE_ID_KEY;
import static org.mockito.Mockito.when;

/**
 * @author tiger
 */
public class SentinelZuulPreFilterTest {

    private String SERVICE_ID = "servicea";

    private String URI = "/servicea/test";

    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getContextPath()).thenReturn("");
        when(httpServletRequest.getPathInfo()).thenReturn(URI);
        RequestContext requestContext = new RequestContext();
        requestContext.set(SERVICE_ID_KEY, SERVICE_ID);
        requestContext.setRequest(httpServletRequest);
        RequestContext.testSetCurrentContext(requestContext);
    }

    @Test
    public void testFilterType() throws Exception {
        SentinelZuulPreFilter sentinelZuulPreFilter = new SentinelZuulPreFilter();
        Assert.assertEquals(sentinelZuulPreFilter.filterType(), PRE_TYPE);
    }

}