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
        long flowId = requestData.getFlowId();
        String luaSha = LuaUtil.loadLuaShaIfNeed(luaId, new RedisScriptLoader() {
            public String load(String luaCode, String slotKey) {
                return jedis.scriptLoad(luaCode);
            }
        });

        Object evalResult = jedis.evalsha(luaSha, Arrays.asList(
                LuaUtil.toTokenParam(requestData.getNamespace(), flowId),
                LuaUtil.toTokenParam(requestData.getNamespace(), flowId, requestData.getAcquireCount())
        ), new ArrayList<String>());

        return LuaUtil.toTokenStatus(evalResult);
    }

    public void resetRedisRuleAndMetrics(String namespace, FlowRule rule) {
        ClusterFlowConfig clusterFlowConfig = rule.getClusterConfig();

        Map<String, String> config = new HashMap<>(4);
        config.put(SAMPLE_COUNT_KEY, String.valueOf(clusterFlowConfig.getSampleCount()));
        config.put(INTERVAL_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()));
        config.put(WINDOW_LENGTH_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()/clusterFlowConfig.getSampleCount()));
        config.put(THRESHOLD_COUNT_KEY, String.valueOf(rule.getCount()));

        Pipeline pipeline = jedis.pipelined();
        pipeline.hset(LuaUtil.toConfigKey(namespace, clusterFlowConfig.getFlowId()), config);
        pipeline.del(LuaUtil.toTokenKey(namespace, clusterFlowConfig.getFlowId()));
        pipeline.sync();
        pipeline.close();
    }

    @Override
    public void clearRuleAndMetrics(String namespace, Set<Long> flowIds) {
        if(flowIds == null || flowIds.isEmpty()) {
            return;
        }

        String[] delConfigKeys = new String[flowIds.size()];
        String[] delTokenKeys = new String[flowIds.size()];

        int i = 0;
        for (Long flowId : flowIds) {
            delTokenKeys[i] = LuaUtil.toConfigKey(namespace, flowId);
            delConfigKeys[i] = LuaUtil.toTokenKey(namespace, flowId);
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
