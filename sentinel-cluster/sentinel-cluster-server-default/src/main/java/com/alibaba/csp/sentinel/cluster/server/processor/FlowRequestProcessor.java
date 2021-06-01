/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.server.processor;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenService;
import com.alibaba.csp.sentinel.cluster.annotation.RequestType;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.data.FlowRequestData;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.FlowTokenResponseData;
import com.alibaba.csp.sentinel.cluster.server.TokenServiceProvider;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@RequestType(ClusterConstants.MSG_TYPE_FLOW)
public class FlowRequestProcessor implements RequestProcessor<FlowRequestData, FlowTokenResponseData> {

    @Override
    public ClusterResponse<FlowTokenResponseData> processRequest(ClusterRequest<FlowRequestData> request) {
        TokenService tokenService = TokenServiceProvider.getService();

        long flowId = request.getData().getFlowId();
        int count = request.getData().getCount();
        boolean prioritized = request.getData().isPriority();

        TokenResult result = tokenService.requestToken(flowId, count, prioritized);
        return toResponse(result, request);
    }

    private ClusterResponse<FlowTokenResponseData> toResponse(TokenResult result, ClusterRequest request) {
        return new ClusterResponse<>(request.getId(), request.getType(), result.getStatus(),
            new FlowTokenResponseData()
                .setRemainingCount(result.getRemaining())
                .setWaitInMs(result.getWaitInMs())
        );
    }
}
