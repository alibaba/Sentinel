package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;

import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.function.Function;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.*;

import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;
import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.THRESHOLD_COUNT_KEY;

public class JedisClient implements RedisClient {
    private Jedis jedis;

    public JedisClient(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public int requestToken(String luaId, RequestData requestData) {
        String luaCode = LuaUtil.loadLuaCodeIfNeed(luaId);
        String luaSha = LuaUtil.loadLuaShaIfNeed(luaCode, new Function<String, String>() {
            public String apply(String s) {
                return jedis.scriptLoad(s);
            }
        });

        String flowId = String.valueOf(requestData.getFlowId());
        Object evalResult = jedis.evalsha(luaSha, Arrays.asList(
                flowId,
                LuaUtil.toLuaParam(requestData.getAcquireCount(), flowId)
        ), new ArrayList<String>());

        if(evalResult == null) {
            return TokenResultStatus.FAIL;
        } else {
            if(Integer.parseInt(evalResult.toString()) > 0) {
                return TokenResultStatus.OK;
            } else {
                return TokenResultStatus.BLOCKED;
            }
        }
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
