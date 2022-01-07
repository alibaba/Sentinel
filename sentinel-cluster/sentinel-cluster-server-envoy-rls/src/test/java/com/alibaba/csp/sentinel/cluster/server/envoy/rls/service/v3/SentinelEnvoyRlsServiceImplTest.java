package com.alibaba.csp.sentinel.cluster.server.envoy.rls.service.v3;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import io.envoyproxy.envoy.extensions.common.ratelimit.v3.RateLimitDescriptor;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitRequest;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitResponse;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * Created by Winjay
 *
 * @author Winjay chan
 * @date 2021/8/13 16:31
 */
public class SentinelEnvoyRlsServiceImplTest {
    @Test
    public void testShouldRateLimitPass() {
        SentinelEnvoyRlsServiceImpl rlsService = mock(SentinelEnvoyRlsServiceImpl.class);
        StreamObserver<RateLimitResponse> streamObserver = mock(StreamObserver.class);
        String domain = "testShouldRateLimitPass";
        int acquireCount = 1;

        RateLimitDescriptor descriptor1 = RateLimitDescriptor.newBuilder()
                .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("rk1").setValue("rv1").build())
                .build();
        RateLimitDescriptor descriptor2 = RateLimitDescriptor.newBuilder()
                .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("rk2").setValue("rv2").build())
                .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("rk3").setValue("rv3").build())
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
        assertEquals(RateLimitResponse.Code.OK, response.getOverallCode());
        response.getStatusesList()
                .forEach(e -> assertEquals(RateLimitResponse.Code.OK, e.getCode()));
    }

    @Test
    public void testShouldRatePartialBlock() {
        SentinelEnvoyRlsServiceImpl rlsService = mock(SentinelEnvoyRlsServiceImpl.class);
        StreamObserver<RateLimitResponse> streamObserver = mock(StreamObserver.class);
        String domain = "testShouldRatePartialBlock";
        int acquireCount = 1;

        RateLimitDescriptor descriptor1 = RateLimitDescriptor.newBuilder()
                .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("rk1").setValue("rv1").build())
                .build();
        RateLimitDescriptor descriptor2 = RateLimitDescriptor.newBuilder()
                .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("rk2").setValue("rv2").build())
                .addEntries(RateLimitDescriptor.Entry.newBuilder().setKey("rk3").setValue("rv3").build())
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
        assertEquals(RateLimitResponse.Code.OVER_LIMIT, response.getOverallCode());
        assertEquals(2, response.getStatusesCount());
        assertTrue(response.getStatusesList().stream()
                .anyMatch(e -> e.getCode().equals(RateLimitResponse.Code.OVER_LIMIT)));
        assertFalse(response.getStatusesList().stream()
                .allMatch(e -> e.getCode().equals(RateLimitResponse.Code.OVER_LIMIT)));
    }
}
