package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.redis.RedisProcessor;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.lua.RedisScriptLoader;
import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;

import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.*;

import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;
import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.THRESHOLD_COUNT_KEY;

public class JedisProcessor implements RedisProcessor {
    private Jedis jedis;

    public JedisProcessor(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public int requestToken(String luaId, RequestData requestData) {
        String luaSha = LuaUtil.loadLuaShaIfNeed(luaId, requestData.getFlowId(), new RedisScriptLoader() {
            public String load(String luaCode, long flowId) {
                return jedis.scriptLoad(luaCode);
            }
        });

        String flowIdStr = String.valueOf(requestData.getFlowId());
        Object evalResult = jedis.evalsha(luaSha, Arrays.asList(
                flowIdStr,
                LuaUtil.toLuaParam(requestData.getAcquireCount(), flowIdStr)
        ), new ArrayList<String>());

        return LuaUtil.toTokenStatus(evalResult);
    }

    public void resetRedisRuleAndMetrics(FlowRule rule) {
        ClusterFlowConfig clusterFlowConfig = rule.getClusterConfig();

        Map<String, String> config = new HashMap<>();
        config.put(SAMPLE_COUNT_KEY, String.valueOf(clusterFlowConfig.getSampleCount()));
        config.put(INTERVAL_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()));
        config.put(WINDOW_LENGTH_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()/clusterFlowConfig.getSampleCount()));
        config.put(THRESHOLD_COUNT_KEY, String.valueOf(rule.getCount()));

        Pipeline pipeline = jedis.pipelined();
        pipeline.hset(LuaUtil.toLuaParam(FLOW_RULE_CONFIG_KEY, clusterFlowConfig.getFlowId()), config);
        pipeline.del(LuaUtil.toLuaParam(FLOW_CHECKER_TOKEN_KEY, clusterFlowConfig.getFlowId()));
        pipeline.sync();
        pipeline.close();
    }

    @Override
    public void clearRuleAndMetrics(Set<Long> flowIds) {
        if(flowIds == null || flowIds.isEmpty()) {
            return;
        }

        String[] delConfigKeys = new String[flowIds.size()];
        String[] delTokenKeys = new String[flowIds.size()];

        int i = 0;
        for (Long flowId : flowIds) {
            delConfigKeys[i] = LuaUtil.toLuaParam(FLOW_RULE_CONFIG_KEY, flowId);
            delTokenKeys[i] = LuaUtil.toLuaParam(FLOW_CHECKER_TOKEN_KEY, flowId);
            i++;
        }
        // todo 考虑批量操作导致阻塞
        jedis.del(delConfigKeys);
        jedis.del(delTokenKeys);
    }

    @Override
    public void close() {
        jedis.close();
    }

}
