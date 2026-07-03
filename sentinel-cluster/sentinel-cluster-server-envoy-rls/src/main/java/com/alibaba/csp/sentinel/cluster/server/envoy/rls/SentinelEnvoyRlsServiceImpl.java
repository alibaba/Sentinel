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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.flow.SimpleClusterFlowChecker;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.log.RlsAccessLogger;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule.EnvoySentinelRuleConverter;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import com.google.protobuf.TextFormat;
import io.envoyproxy.envoy.api.v2.ratelimit.RateLimitDescriptor;
import io.envoyproxy.envoy.api.v2.ratelimit.RateLimitDescriptor.Entry;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitRequest;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitResponse;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitResponse.Code;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitResponse.DescriptorStatus;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitResponse.RateLimit;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitResponse.RateLimit.Unit;
import io.envoyproxy.envoy.service.ratelimit.v2.RateLimitServiceGrpc;
import io.grpc.stub.StreamObserver;

import static com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule.EnvoySentinelRuleConverter.SEPARATOR;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public class SentinelEnvoyRlsServiceImpl extends RateLimitServiceGrpc.RateLimitServiceImplBase {

    @Override
    public void shouldRateLimit(RateLimitRequest request, StreamObserver<RateLimitResponse> responseObserver) {
        int acquireCount = request.getHitsAddend();
        if (acquireCount < 0) {
            responseObserver.onError(new IllegalArgumentException(
                "acquireCount should be positive, but actual: " + acquireCount));
            return;
        }
        if (acquireCount == 0) {
            // Not present, use the default "1" by default.
            acquireCount = 1;
        }

        String domain = request.getDomain();
        boolean blocked = false;
        List<DescriptorStatus> statusList = new ArrayList<>(request.getDescriptorsCount());
        for (RateLimitDescriptor descriptor : request.getDescriptorsList()) {
            Tuple2<FlowRule, TokenResult> t = checkToken(domain, descriptor, acquireCount);
            TokenResult r = t.r2;

            printAccessLogIfNecessary(domain, descriptor, r);

            if (r.getStatus() == TokenResultStatus.NO_RULE_EXISTS) {
                // If the rule of the descriptor is absent, the request will pass directly.
                r.setStatus(TokenResultStatus.OK);
            }

            if (!blocked && r.getStatus() != TokenResultStatus.OK) {
                blocked = true;
            }

            Code statusCode = r.getStatus() == TokenResultStatus.OK ? Code.OK : Code.OVER_LIMIT;
            DescriptorStatus.Builder descriptorStatusBuilder = DescriptorStatus.newBuilder()
                .setCode(statusCode);
            if (t.r1 != null) {
                descriptorStatusBuilder
                    .setCurrentLimit(RateLimit.newBuilder().setUnit(Unit.SECOND)
                        .setRequestsPerUnit((int)t.r1.getCount())
                        .build())
                    .setLimitRemaining(r.getRemaining());
            }
            statusList.add(descriptorStatusBuilder.build());
        }

        Code overallStatus = blocked ? Code.OVER_LIMIT : Code.OK;
        RateLimitResponse response = RateLimitResponse.newBuilder()
            .setOverallCode(overallStatus)
            .addAllStatuses(statusList)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void printAccessLogIfNecessary(String domain, RateLimitDescriptor descriptor, TokenResult result) {
        if (!RlsAccessLogger.isEnabled()) {
            return;
        }
        String message = new StringBuilder("[RlsAccessLog] domain=").append(domain)
            .append(", descriptor=").append(TextFormat.shortDebugString(descriptor))
            .append(", checkStatus=").append(result.getStatus())
            .append(", remaining=").append(result.getRemaining())
            .toString();
        RlsAccessLogger.log(message);
    }

    protected Tuple2<FlowRule, TokenResult> checkToken(String domain, RateLimitDescriptor descriptor, int acquireCount) {
        long ruleId = EnvoySentinelRuleConverter.generateFlowId(generateKey(domain, descriptor));

        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(ruleId);
        if (rule == null) {
            // Pass if the target rule is absent.
            return Tuple2.of(null, new TokenResult(TokenResultStatus.NO_RULE_EXISTS));
        }
        // If the rule is present, it should be valid.
        return Tuple2.of(rule, SimpleClusterFlowChecker.acquireClusterToken(rule, acquireCount));
    }

    private String generateKey(String domain, RateLimitDescriptor descriptor) {
        StringBuilder sb = new StringBuilder(domain);
        for (Entry resource : descriptor.getEntriesList()) {
            sb.append(SEPARATOR).append(resource.getKey()).append(SEPARATOR).append(resource.getValue());
        }
        return sb.toString();
    }
}
