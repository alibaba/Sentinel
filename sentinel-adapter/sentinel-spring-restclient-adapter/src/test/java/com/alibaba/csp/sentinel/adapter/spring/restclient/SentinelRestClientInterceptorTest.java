/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.restclient;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SentinelRestClientInterceptorTest {

    private final SentinelRestClientInterceptor interceptor = new SentinelRestClientInterceptor();
    private final MockClientHttpRequest request = new MockClientHttpRequest(
            HttpMethod.GET, URI.create("http://localhost/status"));
    private final ClientHttpResponse response = mock(ClientHttpResponse.class);

    @Test
    public void closesResponseWhenStatusReadFails() throws IOException {
        IOException statusFailure = new IOException("status unavailable");
        when(response.getStatusCode()).thenThrow(statusFailure);

        IOException thrown = interceptExpectingIOException();

        assertSame(statusFailure, thrown);
        verify(response).close();
    }

    @Test
    public void preservesStatusFailureWhenClosingResponseFails() throws IOException {
        IOException statusFailure = new IOException("status unavailable");
        IllegalStateException closeFailure = new IllegalStateException("close failed");
        when(response.getStatusCode()).thenThrow(statusFailure);
        doThrow(closeFailure).when(response).close();

        IOException thrown = interceptExpectingIOException();

        assertSame(statusFailure, thrown);
        assertArrayEquals(new Throwable[]{closeFailure}, thrown.getSuppressed());
    }

    @Test
    public void returnsServerErrorResponseWithoutClosingIt() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        ClientHttpResponse returned = interceptor.intercept(request, new byte[0], (req, body) -> response);

        assertSame(response, returned);
        verify(response, never()).close();
    }

    private IOException interceptExpectingIOException() {
        try {
            interceptor.intercept(request, new byte[0], (req, body) -> response);
        } catch (IOException ex) {
            return ex;
        }
        throw new AssertionError("Expected an IOException while reading the response status");
    }
}
