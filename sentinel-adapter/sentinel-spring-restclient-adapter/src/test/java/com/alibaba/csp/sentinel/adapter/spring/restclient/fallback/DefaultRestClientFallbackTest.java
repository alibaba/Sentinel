package com.alibaba.csp.sentinel.adapter.spring.restclient.fallback;

import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import java.net.URI;

import static org.junit.Assert.assertSame;

/**
 * Tests for {@link DefaultRestClientFallback}.
 *
 * @author uuuyuqi
 */
public class DefaultRestClientFallbackTest {

	@Test(expected = SentinelRpcException.class)
	public void testHandleThrowsException() {
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
		fallback.handle(request, new byte[0], null, ex);
	}

	@Test
	public void testHandleWrapsBlockException() {
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
		try {
			fallback.handle(request, new byte[0], null, ex);
		} catch (SentinelRpcException e) {
			assertSame(ex, e.getCause());
		}
	}
}