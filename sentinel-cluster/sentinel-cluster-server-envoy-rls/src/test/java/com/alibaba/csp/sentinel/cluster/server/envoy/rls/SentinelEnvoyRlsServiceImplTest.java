/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import io.envoyproxy.envoy.api.v2.ratelimit.RateLimitDescriptor;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitRequest;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitResponse;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitResponse.Code;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Zhao
 */
public class SentinelEnvoyRlsServiceImplTest {

    @Test
    public void testShouldRateLimitPass() {
        SentinelEnvoyRlsServiceImpl rlsService = mock(SentinelEnvoyRlsServiceImpl.class);
        StreamObserver<RateLimitResponse> streamObserver = mock(StreamObserver.class);
        String domain = "testShouldRateLimitPass";
        int acquireCount = 1;

        RateLimitDescriptor descriptor1 = RateLimitDescriptor.newBuilder()
            .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("a1").setValue("b1").build())
            .build();
        RateLimitDescriptor descriptor2 = RateLimitDescriptor.newBuilder()
            .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("a2").setValue("b2").build())
            .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("a3").setValue("b3").build())
            .build();

        ArgumentCaptor<RateLimitResponse> responseCapture = ArgumentCaptor.forClass(RateLimitResponse.class);
        doNothing().when(streamObserver)
            .onNext(responseCapture.capture());

        doCallRealMethod().when(rlsService).shouldRateLimit(any(), any());
        when(rlsService.checkToken(eq(domain), same(descriptor1), eq(acquireCount)))
            .thenReturn(Tuple2.of(new FlowRule(), new TokenResult(TokenResultStatus.OK)));
        when(rlsService.checkToken(eq(domain), same(descriptor2), eq(acquireCount)))
            .thenReturn(Tuple2.of(new FlowRule(), new TokenResult(TokenResultStatus.OK)));

        RateLimitRequest rateLimitRequest = RateLimitRequest.newBuilder()
            .addDescriptors(descriptor1)
            .addDescriptors(descriptor2)
            .setDomain(domain)
            .setHitsAddend(acquireCount)
            .build();
        rlsService.shouldRateLimit(rateLimitRequest, streamObserver);

        RateLimitResponse response = responseCapture.getValue();
        assertEquals(Code.OK, response.getOverallCode());
        response.getStatusesList()
            .forEach(e -> assertEquals(Code.OK, e.getCode()));
    }

    @Test
    public void testShouldRatePartialBlock() {
        SentinelEnvoyRlsServiceImpl rlsService = mock(SentinelEnvoyRlsServiceImpl.class);
        StreamObserver<RateLimitResponse> streamObserver = mock(StreamObserver.class);
        String domain = "testShouldRatePartialBlock";
        int acquireCount = 1;

        RateLimitDescriptor descriptor1 = RateLimitDescriptor.newBuilder()
            .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("a1").setValue("b1").build())
            .build();
        RateLimitDescriptor descriptor2 = RateLimitDescriptor.newBuilder()
            .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("a2").setValue("b2").build())
            .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("a3").setValue("b3").build())
            .build();

        ArgumentCaptor<RateLimitResponse> responseCapture = ArgumentCaptor.forClass(RateLimitResponse.class);
        doNothing().when(streamObserver)
            .onNext(responseCapture.capture());

        doCallRealMethod().when(rlsService).shouldRateLimit(any(), any());
        when(rlsService.checkToken(eq(domain), same(descriptor1), eq(acquireCount)))
            .thenReturn(Tuple2.of(new FlowRule(), new TokenResult(TokenResultStatus.BLOCKED)));
        when(rlsService.checkToken(eq(domain), same(descriptor2), eq(acquireCount)))
            .thenReturn(Tuple2.of(new FlowRule(), new TokenResult(TokenResultStatus.OK)));

        RateLimitRequest rateLimitRequest = RateLimitRequest.newBuilder()
            .addDescriptors(descriptor1)
            .addDescriptors(descriptor2)
            .setDomain(domain)
            .setHitsAddend(acquireCount)
            .build();
        rlsService.shouldRateLimit(rateLimitRequest, streamObserver);

        RateLimitResponse response = responseCapture.getValue();
        assertEquals(Code.OVER_LIMIT, response.getOverallCode());
        assertEquals(2, response.getStatusesCount());
        assertTrue(response.getStatusesList().stream()
            .anyMatch(e -> e.getCode().equals(Code.OVER_LIMIT)));
        assertFalse(response.getStatusesList().stream()
            .allMatch(e -> e.getCode().equals(Code.OVER_LIMIT)));
    }
}
