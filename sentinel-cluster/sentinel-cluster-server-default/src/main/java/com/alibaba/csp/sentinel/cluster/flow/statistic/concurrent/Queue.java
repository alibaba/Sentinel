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
public class Queue {
    private static BlockingQueue<RequestObject> blockingQueue = new ArrayBlockingQueue<>(100);

    private static volatile int FLAG = 0;

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ExecutorService consumerPool = Executors.newFixedThreadPool(1);

    static {
        tryToConsumeRequestInQueue();
    }

    public static void addRequestToWaitQueue(final RequestObject requestObject) {
        try {
            blockingQueue.offer(requestObject, 2L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void tryToConsumeRequestInQueue() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (FLAG == 0) {
                    TokenResult tokenResult = null;
                    RequestObject res = null;
                    try {
                        res = blockingQueue.poll(2L, TimeUnit.SECONDS);
                        if (res == null) {
                            continue;
                        }
                        if(System.currentTimeMillis()-res.getCreatTime()>1000){
                            sendResponse(res.getCtx(),new TokenResult(TokenResultStatus.BLOCKED),res.getRequest());
                            continue;
                        }
                        long flowId = res.getRequest().getData().getFlowId();
                        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(flowId);
                        int acquire = res.getRequest().getData().getCount();
                        AtomicInteger nowCalls = CurrentConcurrencyManager.get(flowId);
                        if (nowCalls == null) {
                            sendResponse(res.getCtx(),new TokenResult(TokenResultStatus.FAIL),res.getRequest());
                            continue;
                        }
                        synchronized (nowCalls) {
                            while (nowCalls.get() + acquire > ConcurrentClusterFlowChecker.calcGlobalThreshold(rule)) {
                                Thread.sleep(2);
                            }
                            nowCalls.getAndAdd(acquire);
                        }
                        TokenCacheNode node = TokenCacheNode.generateTokenCacheNode(rule, acquire, res.getAddress());
                        TokenCacheNodeManager.putTokenCacheNode(node.getTokenId(), node);
                        tokenResult = new TokenResult(TokenResultStatus.OK);
                        tokenResult.setTokenId(node.getTokenId());
                        sendResponse(res.getCtx(),tokenResult,res.getRequest());
                    } catch (InterruptedException e) {
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

    private static  void sendResponse(ChannelHandlerContext ctx, TokenResult result, ClusterRequest<ConcurrentFlowAcquireRequestData> request){
        ClusterResponse<ConcurrentFlowAcquireResponseData> response = toResponse(result,request);
        ctx.writeAndFlush(response);
    }
}
