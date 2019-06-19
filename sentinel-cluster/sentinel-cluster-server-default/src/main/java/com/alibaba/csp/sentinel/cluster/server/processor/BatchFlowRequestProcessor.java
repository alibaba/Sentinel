/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.server.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenService;
import com.alibaba.csp.sentinel.cluster.annotation.RequestType;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.data.BatchFlowRequestData;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.BatchFlowTokenResponseData;
import com.alibaba.csp.sentinel.cluster.server.TokenServiceProvider;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
@RequestType(ClusterConstants.MSG_TYPE_BATCH_FLOW)
public class BatchFlowRequestProcessor implements RequestProcessor<BatchFlowRequestData, BatchFlowTokenResponseData> {

    @Override
    public ClusterResponse<BatchFlowTokenResponseData> processRequest(ClusterRequest<BatchFlowRequestData> request) {
        TokenService tokenService = TokenServiceProvider.getService();

        // The flow ID set should be valid.
        Set<Long> flowIds = request.getData().getFlowIds();
        int count = request.getData().getCount();
        boolean prioritized = request.getData().isPriority();

        TokenResult result = tokenService.batchRequestToken(flowIds, count, prioritized);

        return ClusterResponseGenerator.toBatchFlowResponse(result, request);
    }
}
