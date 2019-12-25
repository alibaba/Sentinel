package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.cluster.client.ClusterTokenClient;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisClientFactoryManager;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import java.util.Collection;

public class RedisClusterTokenService implements ClusterTokenClient {

    @Override
    public TokenResult requestToken(Long flowId, int acquireCount, boolean prioritized) {
        if (notValidRequest(flowId, acquireCount)) {
            return badRequest();
        }

        RedisClientFactory redisClientFactory = RedisClientFactoryManager.getFactory();
        if(redisClientFactory == null) {
            RecordLog.warn(
                    "[RedisClusterTokenService]  cannot get RedisClientFactory, please init redis config");
            return clientFail();
        }

        RedisClient redisClient = redisClientFactory.getClient();

        FlowRule rule = getFlowRule(flowId);
        if (rule == null) {
            RecordLog.warn(
                    "[RedisClusterTokenService] Ignoring invalid flow rule :" + flowId);
            return new TokenResult(TokenResultStatus.NO_RULE_EXISTS);
        }

        int rs = redisClient.executeLua(LuaUtil.FLOW_CHECKER_LUA, createRequestData(flowId, acquireCount));
        redisClient.close();
        return new TokenResult(rs)
                .setRemaining(0)
                .setWaitInMs(0);
    }

    private RequestData createRequestData(Long flowId, int acquireCount) {
        RequestData requestData = new RequestData();
        requestData.setFlowId(flowId);
        requestData.setAcquireCount(acquireCount);
        return requestData;
    }

    @Override
    public TokenResult requestParamToken(Long aLong, int i, Collection<Object> collection) {
        return null;
    }


    private FlowRule getFlowRule(Long flowId) {
        return RedisFlowRuleManager.getFlowRule(flowId);
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
