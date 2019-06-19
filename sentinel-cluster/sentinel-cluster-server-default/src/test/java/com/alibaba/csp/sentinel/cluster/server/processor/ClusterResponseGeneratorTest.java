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

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.BatchFlowTokenResponseData;
import com.alibaba.csp.sentinel.cluster.response.data.FlowTokenResponseData;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class ClusterResponseGeneratorTest {

    @Test
    public void testGenerateForBadRequest() {
        int tid = 337788;
        int type = 33;
        ClusterRequest<?> request = new ClusterRequest<>(tid, type, null);
        ClusterResponse<?> response = ClusterResponseGenerator.badRequest(request);
        assertThat(response.getId()).isEqualTo(tid);
        assertThat(response.getType()).isEqualTo(type);
        assertThat(response.getStatus()).isEqualTo(TokenResultStatus.BAD_REQUEST);
    }

    @Test
    public void testGenerateFlowResponse() {
        int tid = 337789;
        int type = 34;
        ClusterRequest<?> request = new ClusterRequest<>(tid, type, null);

        TokenResult result = new TokenResult().setStatus(TokenResultStatus.OK)
            .setRemaining(127)
            .setWaitInMs(0);
        ClusterResponse<FlowTokenResponseData> response = ClusterResponseGenerator.toFlowResponse(result, request);
        assertThat(response.getId()).isEqualTo(tid);
        assertThat(response.getType()).isEqualTo(type);
        assertThat(response.getStatus()).isEqualTo(TokenResultStatus.OK);
        assertThat(response.getData().getRemainingCount()).isEqualTo(127);
    }

    @Test
    public void testGenerateBatchSuccessResponse() {
        int tid = 337789;
        int type = 34;
        ClusterRequest<?> request = new ClusterRequest<>(tid, type, null);

        TokenResult result = new TokenResult(TokenResultStatus.OK).setRemaining(12);

        ClusterResponse<BatchFlowTokenResponseData> response = ClusterResponseGenerator
            .toBatchFlowResponse(result, request);
        assertThat(response.getId()).isEqualTo(tid);
        assertThat(response.getType()).isEqualTo(type);
        assertThat(response.getStatus()).isEqualTo(TokenResultStatus.OK);
        assertThat(response.getData().getWaitInMs()).isZero();
        assertThat(response.getData().getBlockId()).isNull();
    }

    @Test
    public void testGenerateBatchBlockResponse() {
        int tid = 337789;
        int type = 34;
        ClusterRequest<?> request = new ClusterRequest<>(tid, type, null);

        final long blockId = 123L;
        TokenResult result = new TokenResult(TokenResultStatus.BLOCKED).setRemaining(12)
            .setAttachments(new HashMap<String, Object>() {{ put(ServerConstants.ATTR_KEY_BLOCK_ID, blockId); }});

        ClusterResponse<BatchFlowTokenResponseData> response = ClusterResponseGenerator
            .toBatchFlowResponse(result, request);
        assertThat(response.getId()).isEqualTo(tid);
        assertThat(response.getType()).isEqualTo(type);
        assertThat(response.getStatus()).isEqualTo(TokenResultStatus.BLOCKED);
        assertThat(response.getData().getWaitInMs()).isZero();
        assertThat(response.getData().getBlockId()).isEqualTo(blockId);
    }
}
