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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.queue;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.ConcurrentClusterFlowChecker;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNode;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNodeManager;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.data.ConcurrentFlowAcquireRequestData;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.ConcurrentFlowAcquireResponseData;
import com.alibaba.csp.sentinel.cluster.server.log.ClusterServerStatLogUtil;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yunfeiyanggzq
 */
public class BlockRequestWaitQueue {

    private static BlockingQueue<RequestInfoEntity> blockingQueue = new ArrayBlockingQueue<>(1000);

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ExecutorService consumerPool = Executors.newFixedThreadPool(1);

    static {
        tryToConsumeClientRequestInQueue();
    }

    public static boolean addRequestToWaitQueue(final RequestInfoEntity requestObject) {
        return blockingQueue.offer(requestObject);
    }

    public static TokenResult tryToConsumeServerRequestInQueue(String address, int count, long flowId, boolean prioritized) {
        RequestFuture<TokenResult> future = new RequestFuture<TokenResult>();
        ClusterRequest<ConcurrentFlowAcquireRequestData> request = new ClusterRequest<>();
        ConcurrentFlowAcquireRequestData data = new ConcurrentFlowAcquireRequestData();
        data.setCount(count);
        data.setFlowId(flowId);
        data.setPrioritized(prioritized);
        request.setData(data);
        RequestInfoEntity entity = new RequestInfoEntity(address, request, future);
        if (!blockingQueue.offer(entity)) {
            return new TokenResult(TokenResultStatus.BLOCKED);
        }

        TokenResult res = null;
        try {
            res = future.get();
            if (res != null) {
                return res;
            } else {
                return new TokenResult(TokenResultStatus.BLOCKED);
            }
        } catch (Exception e) {
            return new TokenResult(TokenResultStatus.BLOCKED);
        }
    }

    public static void tryToConsumeClientRequestInQueue() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                TokenResult tokenResult = null;
                RequestInfoEntity res = null;
                while (true) {
                    try {
                        boolean timeout = false;
                        res = blockingQueue.poll(2L, TimeUnit.SECONDS);
                        if (res == null) {
                            continue;
                        }
                        long flowId = res.getRequest().getData().getFlowId();
                        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(flowId);
                        if (rule == null) {
                            applyResult(res, new TokenResult(TokenResultStatus.BLOCKED));
                            continue;
                        }
                        int acquireCount = res.getRequest().getData().getCount();
                        long maxWaitTime = rule.getMaxQueueingTimeMs();
                        if (System.currentTimeMillis() - res.getCreatTime() > maxWaitTime) {
                            ClusterServerStatLogUtil.log("concurrent|queue|block|" + flowId, acquireCount);
                            applyResult(res, new TokenResult(TokenResultStatus.BLOCKED));
                            continue;
                        }

                        int acquire = res.getRequest().getData().getCount();
                        AtomicInteger nowCalls = CurrentConcurrencyManager.get(flowId);
                        if (nowCalls == null) {
                            ClusterServerStatLogUtil.log("concurrent|queue|block|" + flowId, acquireCount);
                            applyResult(res, new TokenResult(TokenResultStatus.BLOCKED));
                            continue;
                        }

                        synchronized (nowCalls) {
                            while (nowCalls.get() + acquire > ConcurrentClusterFlowChecker.calcGlobalThreshold(rule)) {
                                if (System.currentTimeMillis() - res.getCreatTime() > maxWaitTime) {
                                    ClusterServerStatLogUtil.log("concurrent|queue|block|" + flowId, acquireCount);
                                    applyResult(res, new TokenResult(TokenResultStatus.BLOCKED));
                                    timeout = true;
                                    break;
                                }
                            }
                            if (timeout) {
                                continue;
                            }
                            nowCalls.getAndAdd(acquire);
                        }
                        TokenCacheNode node = TokenCacheNode.generateTokenCacheNode(rule, acquire, res.getAddress());
                        TokenCacheNodeManager.putTokenCacheNode(node.getTokenId(), node);
                        tokenResult = new TokenResult(TokenResultStatus.OK);
                        tokenResult.setTokenId(node.getTokenId());
                        applyResult(res, tokenResult);
                    } catch (InterruptedException e) {
                        ClusterServerStatLogUtil.log("concurrent|queue|block|" + res.getRequest().getData().getFlowId(), res.getRequest().getData().getCount());
                        applyResult(res, new TokenResult(TokenResultStatus.BLOCKED));
                        e.printStackTrace();
                    }
                }
            }
        };
        consumerPool.execute(task);
    }

    private static ClusterResponse<ConcurrentFlowAcquireResponseData> toResponse(TokenResult result, ClusterRequest request) {
        return new ClusterResponse<>(request.getId(), request.getType(), result.getStatus(),
                new ConcurrentFlowAcquireResponseData().setTokenId(result.getTokenId())
        );
    }

    private static void applyResult(RequestInfoEntity entity, TokenResult result) {
        System.out.println("通过队列发放" + result.getStatus());
        if (entity == null) {
            return;
        }
        if (!entity.isServerRequest()) {
            sendResponse(entity.getCtx(), result, entity.getRequest());
        } else {
            entity.getFuture().setSuccess(result);
        }
    }

    private static void sendResponse(ChannelHandlerContext ctx, TokenResult result, ClusterRequest<ConcurrentFlowAcquireRequestData> request) {
        ClusterResponse<ConcurrentFlowAcquireResponseData> response = toResponse(result, request);
        ctx.writeAndFlush(response);
    }
}
