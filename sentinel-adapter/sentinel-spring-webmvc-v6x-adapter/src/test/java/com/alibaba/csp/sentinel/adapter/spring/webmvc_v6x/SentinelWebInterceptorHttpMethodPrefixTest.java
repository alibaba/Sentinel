package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.web.common.UrlCleaner;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.HandlerMapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * The test for the resource request prefix concatenation in spring-webmvc-v6x.
 *
 * @author ylnxwlp
 */
public class SentinelWebInterceptorHttpMethodPrefixTest {

    private SentinelWebInterceptor interceptor;
    private HttpServletRequest mockRequest;

    @Before
    public void setUp() {
        mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    public void testGetResourceNameWithHttpMethodSpecifyEnabled() {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(true);
        interceptor = new SentinelWebInterceptor(config);
        when(mockRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn("/test/path");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getContextPath()).thenReturn("");
        String resourceName = interceptor.getResourceName(mockRequest);
        assertEquals("POST:/test/path", resourceName);
    }

    @Test
    public void testGetResourceNameWithHttpMethodSpecifyDisabled() {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(false);
        interceptor = new SentinelWebInterceptor(config);
        when(mockRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn("/no/prefix");
        when(mockRequest.getMethod()).thenReturn("DELETE");
        when(mockRequest.getContextPath()).thenReturn("");
        String resourceName = interceptor.getResourceName(mockRequest);
        assertEquals("/no/prefix", resourceName);
    }

    @Test
    public void testGetResourceNameEmptyResourceNameShouldReturnEmptyString() {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(true);
        interceptor = new SentinelWebInterceptor(config);
        when(mockRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn("");
        when(mockRequest.getMethod()).thenReturn("PUT");
        when(mockRequest.getContextPath()).thenReturn("");
        String resourceName = interceptor.getResourceName(mockRequest);
        assertEquals("", resourceName);
    }

    @Test
    public void testGetResourceNameNullResourceNameShouldReturnNull() {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(true);
        interceptor = new SentinelWebInterceptor(config);
        when(mockRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn(null);
        when(mockRequest.getMethod()).thenReturn("PATCH");
        String resourceName = interceptor.getResourceName(mockRequest);
        assertNull(resourceName);
    }

    @Test
    public void testGetResourceNameWithUrlCleaner() {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(true);
        config.setUrlCleaner(new UrlCleaner() {
            @Override
            public String clean(String originUrl) {
                return "/cleaned";
            }
        });
        interceptor = new SentinelWebInterceptor(config);
        when(mockRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn("/dirty/path");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getContextPath()).thenReturn("");
        String resourceName = interceptor.getResourceName(mockRequest);
        assertEquals("GET:/cleaned", resourceName);
    }

    @Test
    public void testGetResourceNameWithContextPath() {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(true);
        config.setContextPathSpecify(true);
        interceptor = new SentinelWebInterceptor(config);
        when(mockRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn("/api/user");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getContextPath()).thenReturn("/myapp");
        String resourceName = interceptor.getResourceName(mockRequest);
        assertEquals("GET:/myapp/api/user", resourceName);
    }

    @Test
    public void testGetResourceNameWithContextPathDisabled() {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(true);
        config.setContextPathSpecify(false);
        interceptor = new SentinelWebInterceptor(config);
        when(mockRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn("/api/user");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getContextPath()).thenReturn("/myapp");
        String resourceName = interceptor.getResourceName(mockRequest);
        assertEquals("GET:/api/user", resourceName);
    }
}