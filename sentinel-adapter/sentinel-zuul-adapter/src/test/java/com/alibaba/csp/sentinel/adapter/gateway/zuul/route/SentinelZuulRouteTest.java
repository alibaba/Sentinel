package com.alibaba.csp.sentinel.adapter.gateway.zuul.route;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.api.route.PrefixRoutePathMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.api.route.RegexRoutePathMatcher;
import com.netflix.zuul.context.RequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant.SERVICE_ID_KEY;

/**
 * @author: jiangzian
 **/
public class SentinelZuulRouteTest {

    private final String SERVICE_ID = "servicea";

    private final String SERVER_NAME = "www.example.com";
    private final String REQUEST_URI = "/servicea/test.jsp";
    private final String QUERY_STRING = "param1=value1&param";

    private RequestContext requestContext = new RequestContext();


    @Before
    public void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName(SERVER_NAME);
        request.setRequestURI(REQUEST_URI);
        request.setQueryString(QUERY_STRING);
        requestContext.set(SERVICE_ID_KEY, SERVICE_ID);
        requestContext.setRequest(request);
        RequestContext.testSetCurrentContext(requestContext);
    }

    @Test
    public void testPrefixRoutePathMatche() {
        PrefixRoutePathMatcher prefixRoutePathMatcher = new PrefixRoutePathMatcher("/servicea/????.jsp");
        Assert.assertTrue(prefixRoutePathMatcher.test(requestContext));

        prefixRoutePathMatcher = new PrefixRoutePathMatcher("/servicea/????.do");
        Assert.assertTrue(!prefixRoutePathMatcher.test(requestContext));
    }

    @Test
    public void testRegexRoutePathMatcher() {
        RegexRoutePathMatcher regexRoutePathMatcher = new RegexRoutePathMatcher("/servicea/[a-zA-z]+(\\.jsp)");
        Assert.assertTrue(regexRoutePathMatcher.test(requestContext));

        regexRoutePathMatcher = new RegexRoutePathMatcher("/serviceb/[a-zA-z]+(\\.jsp)");
        Assert.assertTrue(!regexRoutePathMatcher.test(requestContext));
    }

}