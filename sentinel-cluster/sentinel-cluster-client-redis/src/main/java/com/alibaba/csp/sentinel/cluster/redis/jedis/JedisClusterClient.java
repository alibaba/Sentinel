package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.function.Function;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.util.*;

import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;

public class JedisClusterClient implements RedisClient {
    private JedisCluster jedisCluster;

    public JedisClusterClient(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public int requestToken(String luaCode, RequestData requestData) {
        final String flowId = String.valueOf(requestData.getFlowId());

        final String lua = LuaUtil.loadLuaCodeIfNeed(luaCode);

        int slot = JedisClusterCRC16.getSlot(flowId);  // positioning slots by flowId
        String luaSha = LuaUtil.loadLuaShaIfNeed(slot + lua, new Function<String, String>() {
            public String apply(String s) {
                return jedisCluster.scriptLoad(lua, flowId);
            }
        });

        Object o = jedisCluster.evalsha(luaSha, Arrays.asList(
                flowId,
                LuaUtil.toLuaParam(requestData.getAcquireCount(), flowId)
        ), new ArrayList<String>());
        if(o == null) {
            return TokenResultStatus.FAIL;
        } else {
            if(Integer.parseInt(o.toString()) > 0) {
                return TokenResultStatus.OK;
            } else {
                return TokenResultStatus.BLOCKED;
            }
        }
    }

    @Override
    public void resetFlowMetrics(Set<Long> flowIds) {
        if(flowIds == null || flowIds.isEmpty()) {
            return;
        }

        for (Long flowId : flowIds) {
            jedisCluster.del(LuaUtil.toLuaParam(FLOW_CHECKER_TOKEN_KEY, flowId));
        }
    }

    @Override
    public void publishRule(FlowRule rule) {
        ClusterFlowConfig clusterFlowConfig = rule.getClusterConfig();

        Map<String, String> config = new HashMap<>();
        config.put(SAMPLE_COUNT_KEY, String.valueOf(clusterFlowConfig.getSampleCount()));
        config.put(INTERVAL_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()));
        config.put(WINDOW_LENGTH_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()/clusterFlowConfig.getSampleCount()));
        config.put(THRESHOLD_COUNT_KEY, String.valueOf(rule.getCount()));
        jedisCluster.hset(LuaUtil.toLuaParam(FLOW_RULE_CONFIG_KEY, clusterFlowConfig.getFlowId()), config);
    }

    @Override
    public void clearRule(Set<Long> flowIds) {
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
