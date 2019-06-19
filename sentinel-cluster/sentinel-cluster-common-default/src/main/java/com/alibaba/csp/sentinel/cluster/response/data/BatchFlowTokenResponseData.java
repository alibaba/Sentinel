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
package com.alibaba.csp.sentinel.cluster.response.data;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public class BatchFlowTokenResponseData {

    private int waitInMs;

    /**
     * Flow ID of the rule that triggered flow control.
     */
    private Long blockId;

    public int getWaitInMs() {
        return waitInMs;
    }

    public BatchFlowTokenResponseData setWaitInMs(int waitInMs) {
        this.waitInMs = waitInMs;
        return this;
    }

    public Long getBlockId() {
        return blockId;
    }

    public BatchFlowTokenResponseData setBlockId(Long blockId) {
        this.blockId = blockId;
        return this;
    }

    @Override
    public String toString() {
        return "BatchFlowTokenResponseData{" +
            "waitInMs=" + waitInMs +
            ", blockId=" + blockId +
            '}';
    }
}
