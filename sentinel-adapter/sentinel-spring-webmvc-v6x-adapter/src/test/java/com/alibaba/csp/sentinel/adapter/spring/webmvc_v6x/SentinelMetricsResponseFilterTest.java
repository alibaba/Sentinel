package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util.AdaptiveUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Unit testing for the adaptive fallback filter for the WebMvc adapter
 *
 * @author ylnxwlp
 */
@RunWith(MockitoJUnitRunner.class)
public class SentinelMetricsResponseFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private SentinelMetricsResponseFilter filter;

    @Before
    public void setUp() {
        filter = new SentinelMetricsResponseFilter();
    }

    @Test
    public void testDoFilterNonHttpObjectsShouldPassThrough() throws IOException, ServletException {
        jakarta.servlet.ServletRequest nonHttpRequest = mock(jakarta.servlet.ServletRequest.class);
        jakarta.servlet.ServletResponse nonHttpResponse = mock(jakarta.servlet.ServletResponse.class);

        filter.doFilter(nonHttpRequest, nonHttpResponse, filterChain);

        verify(filterChain).doFilter(nonHttpRequest, nonHttpResponse);
    }

    @Test
    public void testDoFilterMissingHeaderShouldNotInjectMetrics() throws IOException, ServletException {
        when(request.getHeader("X-Sentinel-Adaptive")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    public void testDoFilterHeaderNotEnabledShouldNotInjectMetrics() throws IOException, ServletException {
        when(request.getHeader("X-Sentinel-Adaptive")).thenReturn("disabled");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    public void testDoFilterHeaderEnabledShouldInjectMetrics() throws IOException, ServletException {
        when(request.getHeader("X-Sentinel-Adaptive")).thenReturn("enabled");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<ServletResponse> responseCaptor = ArgumentCaptor.forClass(jakarta.servlet.ServletResponse.class);
        verify(filterChain).doFilter(eq(request), responseCaptor.capture());

        jakarta.servlet.ServletResponse wrappedResponse = responseCaptor.getValue();

        wrappedResponse.getOutputStream();

        String metric = AdaptiveUtils.packServerMetric();
        if (metric != null && !metric.isEmpty()) {
            verify(response).setHeader(eq("X-Server-Metrics"), eq(metric));
        } else {
            verify(response, never()).setHeader(anyString(), anyString());
        }
    }

    @Test
    public void testDoFilterHeaderEnabledShouldInjectOnlyOnce() throws IOException, ServletException {
        when(request.getHeader("X-Sentinel-Adaptive")).thenReturn("enabled");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletResponse> responseCaptor = ArgumentCaptor.forClass(jakarta.servlet.ServletResponse.class);
        verify(filterChain).doFilter(eq(request), responseCaptor.capture());

        jakarta.servlet.ServletResponse wrapped = responseCaptor.getValue();
        wrapped.getOutputStream();
        wrapped.getWriter();

        verify(response, atMost(1)).setHeader(anyString(), anyString());
    }

    @Test
    public void testDoFilterResponseCommittedShouldNotInject() throws IOException, ServletException {
        when(request.getHeader("X-Sentinel-Adaptive")).thenReturn("enabled");
        when(response.isCommitted()).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletResponse> responseCaptor = ArgumentCaptor.forClass(jakarta.servlet.ServletResponse.class);
        verify(filterChain).doFilter(eq(request), responseCaptor.capture());

        jakarta.servlet.ServletResponse wrapped = responseCaptor.getValue();
        wrapped.getOutputStream();

        verify(response, never()).setHeader(anyString(), anyString());
    }
}