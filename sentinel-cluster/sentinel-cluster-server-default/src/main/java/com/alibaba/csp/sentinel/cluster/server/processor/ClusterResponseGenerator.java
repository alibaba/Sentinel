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

import java.util.Map;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.BatchFlowTokenResponseData;
import com.alibaba.csp.sentinel.cluster.response.data.FlowTokenResponseData;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
final class ClusterResponseGenerator {

    /**
     * Generate a cluster response with "bad request" status.
     *
     * @param request the corresponding request
     * @param <R>     arbitrary data type
     * @return generated response
     */
    static <R> ClusterResponse<R> badRequest(ClusterRequest request) {
        return new ClusterResponse<>(request.getId(), request.getType(), TokenResultStatus.BAD_REQUEST, null);
    }

    static ClusterResponse<FlowTokenResponseData> toFlowResponse(TokenResult result, ClusterRequest request) {
        return new ClusterResponse<>(request.getId(), request.getType(), result.getStatus(),
            new FlowTokenResponseData()
                .setRemainingCount(result.getRemaining())
                .setWaitInMs(result.getWaitInMs())
        );
    }

    static ClusterResponse<BatchFlowTokenResponseData> toBatchFlowResponse(TokenResult result, ClusterRequest request) {
        Map<String, Object> attachments = result.getAttachments();
        Long blockId = attachments == null ? null : (Long) attachments.get(ServerConstants.ATTR_KEY_BLOCK_ID);
        return new ClusterResponse<>(request.getId(), request.getType(), result.getStatus(),
            new BatchFlowTokenResponseData()
                .setWaitInMs(result.getWaitInMs())
                .setBlockId(blockId)
        );
    }

    //static ClusterResponse<BatchFlowTokenResponseData> toBatchFlowResponse(Map<Long, TokenResult> resultMap,
    //                                                                       ClusterRequest request) {
    //    int waitInMs = 0;
    //    for (Map.Entry<Long, TokenResult> e : resultMap.entrySet()) {
    //        TokenResult result = e.getValue();
    //        switch (result.getStatus()) {
    //            case TokenResultStatus.OK:
    //                continue;
    //            case TokenResultStatus.SHOULD_WAIT:
    //                waitInMs = Math.max(waitInMs, result.getWaitInMs());
    //                continue;
    //            default:
    //                return new ClusterResponse<>(request.getId(), request.getType(), result.getStatus(),
    //                    new BatchFlowTokenResponseData().setBlockId(e.getKey())
    //                );
    //        }
    //
    //    }
    //
    //    int status = waitInMs > 0 ? TokenResultStatus.SHOULD_WAIT : TokenResultStatus.OK;
    //    return new ClusterResponse<>(request.getId(), request.getType(), status,
    //        new BatchFlowTokenResponseData().setWaitInMs(waitInMs)
    //    );
    //}

    private ClusterResponseGenerator() {}
}
