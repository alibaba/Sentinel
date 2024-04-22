package com.alibaba.csp.sentinel.adapter.spring.webflux.callback;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author cxhello
 * @date 2024/4/21
 */
public class DefaultBlockRequestHandlerTest {

    @Test
    public void testHandleRequest_acceptsHtml_returnsHtmlErrorResponse() {
        // Arrange
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.when(exchange.getRequest()).thenReturn(request);
        Mockito.when(request.getHeaders()).thenReturn(httpHeaders);
        Throwable ex = new RuntimeException("Test Exception");
        Mockito.when(exchange.getRequest().getHeaders().getAccept())
                .thenReturn(Arrays.asList(MediaType.TEXT_HTML));

        // Act
        Mono<ServerResponse> result = new DefaultBlockRequestHandler().handleRequest(exchange, ex);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.statusCode().value());
                    assertEquals(MediaType.TEXT_PLAIN, response.headers().getContentType());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testHandleRequest_acceptsJson_returnsJsonErrorResponse() {
        // Arrange
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.when(exchange.getRequest()).thenReturn(request);
        Mockito.when(request.getHeaders()).thenReturn(httpHeaders);
        Throwable ex = new RuntimeException("Test Exception");
        Mockito.when(exchange.getRequest().getHeaders().getAccept())
                .thenReturn(Arrays.asList(MediaType.APPLICATION_JSON));

        // Act
        Mono<ServerResponse> result = new DefaultBlockRequestHandler().handleRequest(exchange, ex);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.statusCode().value());
                    assertEquals(MediaType.APPLICATION_JSON_UTF8, response.headers().getContentType());
                    return true;
                })
                .verifyComplete();
    }

}
