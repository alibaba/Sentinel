package com.alibaba.csp.sentinel.cluster.redis.init;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisClientFactoryManager;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisFlowRuleManager;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.init.InitOrder;
import com.alibaba.csp.sentinel.log.RecordLog;

@InitOrder(0)
public class RedisClusterClientInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        setToClient();
        initJedisClient();
        addRedisFlowRuleListener();
    }

    private void setToClient() {
        ClusterStateManager.setToClient();
    }

    private void addRedisFlowRuleListener() {
        RedisFlowRuleManager.addRedisFlowRuleListener();
    }

    private void initJedisClient() {
        try {
            RedisClusterClientInitFunc.class.getClassLoader().loadClass("redis.clients.jedis.Jedis");
        } catch (ClassNotFoundException e) {
            RecordLog.warn(
                    "[RedisClusterClientInitFunc]  cannot init jedis client");
            return ;
        }

        RedisClientFactoryManager.setClientType(RedisClientFactoryManager.JEDIS_CLIENT);
    }
}
