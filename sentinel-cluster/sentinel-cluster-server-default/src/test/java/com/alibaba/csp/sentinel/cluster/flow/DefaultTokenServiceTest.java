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
package com.alibaba.csp.sentinel.cluster.flow;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class DefaultTokenServiceTest {

    private final DefaultTokenService tokenService = new DefaultTokenService();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGenerateBatchSuccessTokenResult() {
        Map<Long, TokenResult> resultMap = new HashMap<>();
        resultMap.put(1996L, new TokenResult().setStatus(TokenResultStatus.OK).setRemaining(12));
        resultMap.put(1997L, new TokenResult().setStatus(TokenResultStatus.OK).setRemaining(19));
        resultMap.put(1998L, new TokenResult().setStatus(TokenResultStatus.OK).setRemaining(7));

        TokenResult result = tokenService.generateBatchTokenResult(resultMap);
        assertThat(result.getStatus()).isEqualTo(TokenResultStatus.OK);
        assertThat(result.getWaitInMs()).isZero();
        assertThat(result.getAttachments()).isNull();
    }

    @Test
    public void testGenerateBatchWaitTokenResult() {
        Map<Long, TokenResult> resultMap = new HashMap<>();
        // 2 pass, 2 wait
        resultMap.put(1996L, new TokenResult().setStatus(TokenResultStatus.SHOULD_WAIT).setWaitInMs(350));
        resultMap.put(1997L, new TokenResult().setStatus(TokenResultStatus.OK).setRemaining(19));
        resultMap.put(1998L, new TokenResult().setStatus(TokenResultStatus.SHOULD_WAIT).setWaitInMs(200));
        resultMap.put(1999L, new TokenResult().setStatus(TokenResultStatus.OK).setRemaining(7));

        TokenResult result = tokenService.generateBatchTokenResult(resultMap);
        assertThat(result.getStatus()).isEqualTo(TokenResultStatus.SHOULD_WAIT);
        assertThat(result.getWaitInMs()).isEqualTo(350);
        assertThat(result.getAttachments()).isNull();
    }

    @Test
    public void testGenerateBatchBlockedTokenResult() {
        Map<Long, TokenResult> resultMap = new HashMap<>();
        long blockId = 2997L;
        resultMap.put(2996L, new TokenResult().setStatus(TokenResultStatus.OK).setRemaining(12));
        resultMap.put(blockId, new TokenResult().setStatus(TokenResultStatus.BLOCKED).setRemaining(0));
        resultMap.put(2998L, new TokenResult().setStatus(TokenResultStatus.OK).setRemaining(7));

        TokenResult result = tokenService.generateBatchTokenResult(resultMap);
        assertThat(result.getStatus()).isEqualTo(TokenResultStatus.BLOCKED);
        assertThat(result.getWaitInMs()).isZero();
        assertThat(result.getAttachments()).isNotNull();
        assertThat(result.getAttachments())
            .extracting(ServerConstants.ATTR_KEY_BLOCK_ID)
            .contains(blockId);
    }
}
