package com.alibaba.csp.sentinel.adapter.spring.restclient.extractor;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link DefaultRestClientResourceExtractor}.
 *
 * @author uuuyuqi
 */
public class DefaultRestClientResourceExtractorTest {

    @Test
    public void testExtract() throws Exception {
        DefaultRestClientResourceExtractor extractor = new DefaultRestClientResourceExtractor();
        
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
        
        String resourceName = extractor.extract(request);
        assertNotNull(resourceName);
        assertEquals("GET:https://httpbin.org/get", resourceName);
    }

    @Test
    public void testExtractWithPort() throws Exception {
        DefaultRestClientResourceExtractor extractor = new DefaultRestClientResourceExtractor();
        
        HttpRequest request = new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.POST;
            }

            @Override
            public URI getURI() {
                return URI.create("http://localhost:8080/api/users");
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return new org.springframework.http.HttpHeaders();
            }
        };
        
        String resourceName = extractor.extract(request);
        assertNotNull(resourceName);
        assertEquals("POST:http://localhost:8080/api/users", resourceName);
    }
}