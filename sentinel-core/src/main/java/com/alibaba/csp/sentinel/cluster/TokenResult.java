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
package com.alibaba.csp.sentinel.cluster;

import java.util.Map;

/**
 * Result entity of acquiring cluster flow token.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class TokenResult {

    private Integer status;

    private int remaining;
    private int waitInMs;

    private long tokenId;

    private Map<String, String> attachments;

    public TokenResult() {
    }

    public TokenResult(Integer status) {
        this.status = status;
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }

    public Integer getStatus() {
        return status;
    }

    public TokenResult setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public int getRemaining() {
        return remaining;
    }

    public TokenResult setRemaining(int remaining) {
        this.remaining = remaining;
        return this;
    }

    public int getWaitInMs() {
        return waitInMs;
    }

    public TokenResult setWaitInMs(int waitInMs) {
        this.waitInMs = waitInMs;
        return this;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public TokenResult setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
        return this;
    }

    @Override
    public String toString() {
        return "TokenResult{" +
                "status=" + status +
                ", remaining=" + remaining +
                ", waitInMs=" + waitInMs +
                ", attachments=" + attachments +
                ", tokenId=" + tokenId +
                '}';
    }
}
