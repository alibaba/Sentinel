package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.cluster.client.ClusterTokenClient;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisProcessorFactoryManager;
import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;
import com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import java.util.Collection;

public class RedisClusterTokenService implements ClusterTokenClient {
    @Override
    public TokenResult requestToken(Long flowId, int acquireCount, boolean prioritized) {
        if (notValidRequest(flowId, acquireCount)) {
            return badRequest();
        }

        RedisProcessorFactory redisProcessorFactory = RedisProcessorFactoryManager.getFactory();
        if(redisProcessorFactory == null) {
            RecordLog.warn(
                    "[RedisClusterTokenService]  cannot get RedisProcessorFactory, please init redis config");
            return clientFail();
        }

        RedisProcessor redisProcessor = redisProcessorFactory.getProcessor();

        FlowRule rule = getFlowRule(flowId);
        if (rule == null) {
            RecordLog.warn(
                    "[RedisClusterTokenService] Ignoring invalid flow rule :" + flowId);
            return new TokenResult(TokenResultStatus.NO_RULE_EXISTS);
        }

        int rs = redisProcessor.requestToken(ClientConstants.FLOW_CHECKER_LUA, createRequestData(flowId, acquireCount));
        redisProcessor.close();
        // todo remaining value
        return new TokenResult(rs)
                .setRemaining(0)
                .setWaitInMs(0);
    }

    private RequestData createRequestData(Long flowId, int acquireCount) {
        RequestData requestData = new RequestData();
        requestData.setFlowId(flowId);
        requestData.setAcquireCount(acquireCount);
        requestData.setNamespace(RedisClusterFlowRuleManager.getNamespace(flowId));
        RedisClusterFlowRuleManager.getNamespace(flowId);
        return requestData;
    }

    @Override
    public TokenResult requestParamToken(Long aLong, int i, Collection<Object> collection) {
        throw new UnsupportedOperationException("RedisClusterTokenService cannot supported request param token now");
    }

    private FlowRule getFlowRule(Long flowId) {
        return RedisClusterFlowRuleManager.getFlowRule(flowId);
    }

    private boolean notValidRequest(Long id, int count) {
        return id == null || id <= 0 || count <= 0;
    }

    private TokenResult badRequest() {
        return new TokenResult(TokenResultStatus.BAD_REQUEST);
    }

    private TokenResult clientFail() {
        return new TokenResult(TokenResultStatus.FAIL);
    }

    @Override
    public TokenServerDescriptor currentServer() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public int getState() {
        return 0;
    }
}
