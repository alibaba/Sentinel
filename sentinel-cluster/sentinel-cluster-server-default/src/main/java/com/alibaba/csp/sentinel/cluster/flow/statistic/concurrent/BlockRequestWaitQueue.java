package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.ConcurrentClusterFlowChecker;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.data.ConcurrentFlowAcquireRequestData;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.ConcurrentFlowAcquireResponseData;
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

    private static double rate = 0.7;

    static {
        tryToConsumeClientRequestInQueue();
    }

    public static boolean addRequestToWaitQueue(final RequestInfoEntity requestObject) {
        return blockingQueue.offer(requestObject);
    }

    public static TokenResult tryToConsumeServerRequestInQueue(String address, int count, long flowId, boolean prioritized) throws ExecutionException, InterruptedException {
        RequestFuture future = new RequestFuture();
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
        Object res = future.get();
        if (res instanceof TokenResult) {
            return (TokenResult) res;
        } else {
            return new TokenResult(TokenResultStatus.BLOCKED);
        }
    }

    public static void tryToConsumeClientRequestInQueue() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                TokenResult tokenResult = null;
                RequestInfoEntity res = null;
                RequestFuture future=null;
                while (true) {
                    try {
                        boolean timeout = false;
                        res = blockingQueue.poll(2L, TimeUnit.SECONDS);
                        if (res == null) {
                            continue;
                        }
                        long flowId = res.getRequest().getData().getFlowId();
                        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(flowId);
                        long maxWaitTime = (long) (rule.getMaxQueueingTimeMs() * rate);
                        future=res.getFuture();
                        if (System.currentTimeMillis() - res.getCreatTime() > maxWaitTime) {
                            applyResult(res,new TokenResult(TokenResultStatus.BLOCKED));
                            continue;
                        }

                        int acquire = res.getRequest().getData().getCount();
                        AtomicInteger nowCalls = CurrentConcurrencyManager.get(flowId);
                        if (nowCalls == null) {
                            applyResult(res,new TokenResult(TokenResultStatus.BLOCKED));
                            continue;
                        }
                        synchronized (nowCalls) {
                            while (nowCalls.get() + acquire > ConcurrentClusterFlowChecker.calcGlobalThreshold(rule)) {
                                if (System.currentTimeMillis() - res.getCreatTime() > maxWaitTime) {
                                    applyResult(res,new TokenResult(TokenResultStatus.BLOCKED));
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
                        applyResult(res,tokenResult);
                    } catch (InterruptedException e) {
                        applyResult(res,new TokenResult(TokenResultStatus.BLOCKED));
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
    private static void applyResult(RequestInfoEntity entity,TokenResult result){
        if(entity==null){
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
