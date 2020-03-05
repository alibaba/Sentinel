package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.redis.RedisProcessor;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.lua.RedisScriptLoader;
import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.JedisClusterCRC16;
import java.util.*;

import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;

public class JedisClusterProcessor implements RedisProcessor {
    private JedisCluster jedisCluster;

    public JedisClusterProcessor(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public int requestToken(String luaId, RequestData requestData) {
        String flowIdStr = String.valueOf(requestData.getFlowId());

        String luaSha = LuaUtil.loadLuaShaIfNeed(luaId, requestData.getFlowId(), JedisClusterCRC16.getSlot(flowIdStr),
                new RedisScriptLoader() {
            public String load(String luaCode, long flowId) {
                return jedisCluster.scriptLoad(luaCode, String.valueOf(flowId));
            }
        });

        Object evalResult = jedisCluster.evalsha(luaSha, Arrays.asList(
                flowIdStr,
                LuaUtil.toLuaParam(requestData.getAcquireCount(), flowIdStr)
        ), new ArrayList<String>());
        return LuaUtil.toTokenStatus(evalResult);
    }

    @Override
    public void resetRedisRuleAndMetrics(FlowRule rule) {
        ClusterFlowConfig clusterFlowConfig = rule.getClusterConfig();

        Map<String, String> config = new HashMap<>();
        config.put(SAMPLE_COUNT_KEY, String.valueOf(clusterFlowConfig.getSampleCount()));
        config.put(INTERVAL_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()));
        config.put(WINDOW_LENGTH_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()/clusterFlowConfig.getSampleCount()));
        config.put(THRESHOLD_COUNT_KEY, String.valueOf(rule.getCount()));

        // jedisCluster not support pipeline
        jedisCluster.hset(LuaUtil.toLuaParam(FLOW_RULE_CONFIG_KEY, clusterFlowConfig.getFlowId()), config);
        jedisCluster.del(LuaUtil.toLuaParam(FLOW_CHECKER_TOKEN_KEY, clusterFlowConfig.getFlowId()));
    }

    @Override
    public void clearRuleAndMetrics(Set<Long> flowIds) {
        if(flowIds == null || flowIds.isEmpty()) {
            return;
        }

        for (Long ruleId : flowIds) {
            jedisCluster.del(LuaUtil.toLuaParam(FLOW_RULE_CONFIG_KEY, ruleId));
            jedisCluster.del(LuaUtil.toLuaParam(FLOW_CHECKER_TOKEN_KEY, ruleId));
        }
    }

    @Override
    public void close() {

    }
}
