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
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.netflix.zuul.context.RequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

/**
 * @author tiger
 */
public class SentinelPreFilterTest {

    private String SERVICE_ID = "servicea";

    private String URI = "/servicea/test";

    @Mock
    private RequestAttributes requestAttributes;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ProxyRequestHelper proxyRequestHelper;

    @Mock
    private SentinelPreFilter.MockTestService mockTestService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getContextPath()).thenReturn("");
        when(httpServletRequest.getPathInfo()).thenReturn(URI);
        RequestContext requestContext = new RequestContext();
        requestContext.set(SERVICE_ID_KEY, SERVICE_ID);
        requestContext.setRequest(httpServletRequest);
        RequestContext.testSetCurrentContext(requestContext);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    public void testFilterType() throws Exception {
        SentinelZuulProperties properties = new SentinelZuulProperties();
        ProxyRequestHelper proxyRequestHelper = new ProxyRequestHelper();
        SentinelPreFilter sentinelPreFilter = new SentinelPreFilter(properties, proxyRequestHelper,mockTestService);
        Assert.assertEquals(sentinelPreFilter.filterType(), PRE_TYPE);
    }

    @Test
    public void testRun() throws Exception {
        RequestContext ctx = RequestContext.getCurrentContext();
        SentinelZuulProperties properties = new SentinelZuulProperties();
        ProxyRequestHelper proxyRequestHelper = new ProxyRequestHelper();
        SentinelPreFilter sentinelPreFilter = new SentinelPreFilter(properties, proxyRequestHelper,mockTestService);
        sentinelPreFilter.run();
        Assert.assertNull(ctx.getRouteHost());
        Assert.assertEquals(ctx.get(SERVICE_ID_KEY), SERVICE_ID);
    }

    @Test
    public void testServiceFallBackRun() throws Exception {
        RequestContext ctx = RequestContext.getCurrentContext();
        SentinelZuulProperties properties = new SentinelZuulProperties();
        properties.setEnabled(true);
        SentinelPreFilter sentinelPreFilter = new SentinelPreFilter(properties, proxyRequestHelper,mockTestService);

        given(mockTestService.mockTest(SERVICE_ID)).willAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        throw new FlowException("flow ex");
                    }
                }
        );
        sentinelPreFilter.run();
        Assert.assertNull(ctx.getRouteHost());
        Assert.assertNull(ctx.get(SERVICE_ID_KEY));
    }

    @Test
    public void testUriFallBackRun() throws Exception {
        RequestContext ctx = RequestContext.getCurrentContext();
        SentinelZuulProperties properties = new SentinelZuulProperties();
        properties.setEnabled(true);
        SentinelPreFilter sentinelPreFilter = new SentinelPreFilter(properties, proxyRequestHelper,mockTestService);

        given(mockTestService.mockTest(URI)).willAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        throw new FlowException("flow ex");
                    }
                }
        );
        sentinelPreFilter.run();
        Assert.assertNull(ctx.getRouteHost());
        Assert.assertNull(ctx.get(SERVICE_ID_KEY));
    }

}