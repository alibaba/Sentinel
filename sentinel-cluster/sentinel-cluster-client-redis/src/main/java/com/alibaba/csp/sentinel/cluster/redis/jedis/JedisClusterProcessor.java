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
        long flowId = requestData.getFlowId();

        String flowKey = LuaUtil.toTokenParam(requestData.getNamespace(), flowId);
        String luaSha = LuaUtil.loadLuaShaIfNeed(luaId, flowKey, JedisClusterCRC16.getSlot(flowKey),
                new RedisScriptLoader() {
            public String load(String luaCode, String slotKey) {
                return jedisCluster.scriptLoad(luaCode, slotKey);
            }
        });

        Object evalResult = jedisCluster.evalsha(luaSha, Arrays.asList(
                flowKey,
                LuaUtil.toTokenParam(requestData.getNamespace(), flowId, requestData.getAcquireCount())
        ), new ArrayList<String>());
        return LuaUtil.toTokenStatus(evalResult);
    }

    @Override
    public void resetRedisRuleAndMetrics(String namespace, FlowRule rule) {
        ClusterFlowConfig clusterFlowConfig = rule.getClusterConfig();

        Map<String, String> config = new HashMap<>();
        config.put(SAMPLE_COUNT_KEY, String.valueOf(clusterFlowConfig.getSampleCount()));
        config.put(INTERVAL_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()));
        config.put(WINDOW_LENGTH_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()/clusterFlowConfig.getSampleCount()));
        config.put(THRESHOLD_COUNT_KEY, String.valueOf(rule.getCount()));

        // jedisCluster not support pipeline
        jedisCluster.hset(LuaUtil.toConfigKey(namespace, clusterFlowConfig.getFlowId()), config);
        jedisCluster.del(LuaUtil.toTokenKey(namespace, clusterFlowConfig.getFlowId()));
    }

    @Override
    public void clearRuleAndMetrics(String namespace, Set<Long> flowIds) {
        if(flowIds == null || flowIds.isEmpty()) {
            return;
        }

        for (Long ruleId : flowIds) {
            jedisCluster.del(LuaUtil.toConfigKey(namespace, ruleId));
            jedisCluster.del(LuaUtil.toTokenKey(namespace, ruleId));
        }
    }

    @Override
    public void close() {

    }
}
