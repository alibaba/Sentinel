package com.alibaba.csp.sentinel.adapter.gateway.zuul.route;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.api.route.PrefixRoutePathMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.api.route.RegexRoutePathMatcher;
import com.netflix.zuul.context.RequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant.SERVICE_ID_KEY;
import static org.mockito.Mockito.when;

/**
 * @author: jiangzian
 **/
public class SentinelZuulRouteTest {

    private final String SERVICE_ID = "servicea";

    private final String URI = "/servicea/test";

    @Mock
    private HttpServletRequest httpServletRequest;

    private RequestContext requestContext = new RequestContext();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getContextPath()).thenReturn("");
        when(httpServletRequest.getPathInfo()).thenReturn(URI);
        requestContext.set(SERVICE_ID_KEY, SERVICE_ID);
        requestContext.setRequest(httpServletRequest);
        RequestContext.testSetCurrentContext(requestContext);
    }

    @Test
    public void testPrefixRoutePathMatche() {
        PrefixRoutePathMatcher prefixRoutePathMatcher = new PrefixRoutePathMatcher("/**");
        Assert.assertTrue(prefixRoutePathMatcher.test(requestContext));
    }

    @Test
    public void testRegexRoutePathMatcher() {
        RegexRoutePathMatcher prefixRoutePathMatcher = new RegexRoutePathMatcher(URI);
        Assert.assertTrue(prefixRoutePathMatcher.test(requestContext));
    }
}
