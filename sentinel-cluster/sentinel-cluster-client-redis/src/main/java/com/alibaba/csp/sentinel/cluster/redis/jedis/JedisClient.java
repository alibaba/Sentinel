package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;

import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.function.Function;
import redis.clients.jedis.Jedis;

import java.util.*;

import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;
import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.THRESHOLD_COUNT_KEY;

public class JedisClient implements RedisClient {
    private Jedis jedis;

    public JedisClient(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public int requestToken(String luaCode, RequestData requestData) {
        String lua = LuaUtil.loadLuaCodeIfNeed(luaCode);

        String luaSha = LuaUtil.loadLuaShaIfNeed(lua, new Function<String, String>() {
            public String apply(String s) {
                return jedis.scriptLoad(s);
            }
        });

        String key = String.valueOf(requestData.getFlowId());

        Object evalResult = jedis.evalsha(luaSha, Arrays.asList(
                key,
                LuaUtil.toLuaParam(requestData.getAcquireCount(), key)
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

    @Override
    public void resetFlowMetrics(Set<Long> flowIds) {
        if(flowIds == null || flowIds.isEmpty()) {
            return;
        }

        String[] delTokenKeys = new String[flowIds.size()];
        int i = 0;
        for (Long flowId : flowIds) {
            delTokenKeys[i] = LuaUtil.toLuaParam(FLOW_CHECKER_TOKEN_KEY, flowId);
            i++;
        }
        jedis.del(delTokenKeys);
    }


    public void publishRule(FlowRule rule) {
        ClusterFlowConfig clusterFlowConfig = rule.getClusterConfig();

        Map<String, String> config = new HashMap<>();
        config.put(SAMPLE_COUNT_KEY, String.valueOf(clusterFlowConfig.getSampleCount()));
        config.put(INTERVAL_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()));
        config.put(WINDOW_LENGTH_IN_MS_KEY, String.valueOf(clusterFlowConfig.getWindowIntervalMs()/clusterFlowConfig.getSampleCount()));
        config.put(THRESHOLD_COUNT_KEY, String.valueOf(rule.getCount()));
        jedis.hset(LuaUtil.toLuaParam(FLOW_RULE_CONFIG_KEY, clusterFlowConfig.getFlowId()), config);
        jedis.del(LuaUtil.toLuaParam(FLOW_CHECKER_TOKEN_KEY, clusterFlowConfig.getFlowId()));
    }

    @Override
    public void clearRule(Set<Long> flowIds) {
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
