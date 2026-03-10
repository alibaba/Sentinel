package com.alibaba.csp.sentinel.adapter.spring.restclient.fallback;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link DefaultRestClientFallback}.
 *
 * @author uuuyuqi
 */
public class DefaultRestClientFallbackTest {

    @Test
    public void testHandle() throws IOException {
        DefaultRestClientFallback fallback = new DefaultRestClientFallback();
        
        HttpRequest request = new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.GET;
            }

            @Override
            public URI getURI() {
                return URI.create("https://httpbin.org/get");
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return new org.springframework.http.HttpHeaders();
            }
        };
        
        FlowException ex = new FlowException("test", "default");
        ClientHttpResponse response = fallback.handle(request, new byte[0], null, ex);
        
        assertNotNull(response);
        assertTrue(response.getStatusText().contains("blocked by Sentinel"));
        assertTrue(response.getStatusText().contains("FlowException"));
    }
}