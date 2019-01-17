/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.flow.statistic.limit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public final class GlobalRequestLimiter {

    private static final Map<String, RequestLimiter> GLOBAL_QPS_LIMITER_MAP = new ConcurrentHashMap<>();

    public static void initIfAbsent(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        if (!GLOBAL_QPS_LIMITER_MAP.containsKey(namespace)) {
            GLOBAL_QPS_LIMITER_MAP.put(namespace, new RequestLimiter(ClusterServerConfigManager.getMaxAllowedQps(namespace)));
        }
    }

    public static RequestLimiter getRequestLimiter(String namespace) {
        if (namespace == null) {
            return null;
        }
        return GLOBAL_QPS_LIMITER_MAP.get(namespace);
    }

    public static boolean tryPass(String namespace) {
        if (namespace == null) {
            return false;
        }
        RequestLimiter limiter = GLOBAL_QPS_LIMITER_MAP.get(namespace);
        if (limiter == null) {
            return true;
        }
        return limiter.tryPass();
    }

    public static double getCurrentQps(String namespace) {
        RequestLimiter limiter = getRequestLimiter(namespace);
        if (limiter == null) {
            return 0;
        }
        return limiter.getQps();
    }

    public static double getMaxAllowedQps(String namespace) {
        RequestLimiter limiter = getRequestLimiter(namespace);
        if (limiter == null) {
            return 0;
        }
        return limiter.getQpsAllowed();
    }

    public static void applyMaxQpsChange(double maxAllowedQps) {
        AssertUtil.isTrue(maxAllowedQps >= 0, "max allowed QPS should > 0");
        for (RequestLimiter limiter : GLOBAL_QPS_LIMITER_MAP.values()) {
            if (limiter != null) {
                limiter.setQpsAllowed(maxAllowedQps);
            }
        }
    }

    private GlobalRequestLimiter() {}
}
