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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.UUID;

/**
 * We use TokenCacheNodeManager to store the tokenId, whose the underlying storage structure
 * is ConcurrentLinkedHashMap, Its storage node is TokenCacheNode. In order to operate the nowCalls value when
 * the expired tokenId is deleted regularly, we need to store the flowId in TokenCacheNode.
 *
 * @author yunfeiyanggzq
 */
public class TokenCacheNode {
    /**
     * the TokenId of the token
     */
    private Long tokenId;
    /**
     * the client goes offline detection time
     */
    private Long clientTimeout;
    /**
     * the resource called over time detection time
     */
    private Long resourceTimeout;
    /**
     * the flow rule id  corresponding to the token
     */
    private Long flowId;
    /**
     * the number this token occupied
     */
    private int acquireCount;

    /**
     * the address of the client holds the token.
     */
    private String clientAddress;

    public TokenCacheNode() {
    }

    public static TokenCacheNode generateTokenCacheNode(FlowRule rule, int acquireCount, String clientAddress) {
        TokenCacheNode node = new TokenCacheNode();
        // getMostSignificantBits() returns the most significant 64 bits of this UUID's 128 bit value.
        // The probability of collision is extremely low.
        node.setTokenId(UUID.randomUUID().getMostSignificantBits());
        node.setFlowId(rule.getClusterConfig().getFlowId());
        node.setClientTimeout(rule.getClusterConfig().getClientOfflineTime());
        node.setResourceTimeout(rule.getClusterConfig().getResourceTimeout());
        node.setAcquireCount(acquireCount);
        node.setClientAddress(clientAddress);
        return node;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public Long getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(Long clientTimeout) {
        this.clientTimeout = clientTimeout + System.currentTimeMillis();
    }

    public Long getResourceTimeout() {
        return this.resourceTimeout;
    }

    public void setResourceTimeout(Long resourceTimeout) {
        this.resourceTimeout = resourceTimeout + System.currentTimeMillis();
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public int getAcquireCount() {
        return acquireCount;
    }

    public void setAcquireCount(int acquireCount) {
        this.acquireCount = acquireCount;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    @Override
    public String toString() {
        return "TokenCacheNode{" +
                "tokenId=" + tokenId +
                ", clientTimeout=" + clientTimeout +
                ", resourceTimeout=" + resourceTimeout +
                ", flowId=" + flowId +
                ", acquireCount=" + acquireCount +
                ", clientAddress='" + clientAddress + '\'' +
                '}';
    }
}
