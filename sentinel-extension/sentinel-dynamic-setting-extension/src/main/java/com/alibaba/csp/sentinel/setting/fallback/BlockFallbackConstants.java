/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.setting.fallback;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric Zhao
 * @author guanyu
 */
public final class BlockFallbackConstants {

    public static final int TARGET_RES_TYPE_WEB = 1;
    public static final int TARGET_RES_TYPE_RPC = 2;

    public static final int RPC_BLOCK_CUSTOM_RETURN_OBJ = 0;
    public static final int RPC_BLOCK_CUSTOM_EXCEPTION = 1;

    public static final int CACHE_RPC_FALLBACK_CONFIG = 1;

    public static final int RPC_FALLBACK_MANUAL_OPERATE_MODE = 0;
    public static final int RPC_FALLBACK_AUTO_OPERATE_MODE = 1;

    public static final int BLOCK_TYPE_ALL = 0;
    public static final int BLOCK_TYPE_FLOW = 1;
    public static final int BLOCK_TYPE_CIRCUIT_BREAKING = 2;
    public static final int BLOCK_TYPE_SYSTEM = 3;
    public static final int BLOCK_TYPE_PARAM_FLOW = 4;
    public static final int BLOCK_TYPE_MANUAL_DEGRADE = 5;
    public static final int BLOCK_TYPE_ISOLATION = 6;

    public static final int BLOCK_TYPE_WEB_FLOW = 11;

    public static final String SYSTEM_RESOURCE = "__system_block_exception_resource__";

    /**
     * (resourceType, fallbackBehaviorClass)
     */
    private static final Map<Integer, Class<?>> TARGET_RES_TYPE_MAP = new HashMap<Integer, Class<?>>() {{
        put(TARGET_RES_TYPE_WEB, BlockFallbackConfig.WebBlockFallbackBehavior.class);
        put(TARGET_RES_TYPE_RPC, BlockFallbackConfig.RpcBlockFallbackBehavior.class);
    }};

    private static final Map<Class<?>, Integer> BLOCK_TYPE_MAP = new HashMap<Class<?>, Integer>() {{
        put(FlowException.class, BlockFallbackConstants.BLOCK_TYPE_FLOW);
        put(DegradeException.class, BlockFallbackConstants.BLOCK_TYPE_CIRCUIT_BREAKING);
        put(SystemBlockException.class, BlockFallbackConstants.BLOCK_TYPE_SYSTEM);
        put(ParamFlowException.class, BlockFallbackConstants.BLOCK_TYPE_PARAM_FLOW);
    }};

    public static Integer parseBlockType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return BLOCK_TYPE_MAP.get(clazz);
    }

    public static boolean isResourceTypeSupported(int t) {
        return TARGET_RES_TYPE_MAP.containsKey(t);
    }

    public static boolean isResourceTypeSupportedAndMatch(int t, Object behavior) {
        Class<?> clazz = TARGET_RES_TYPE_MAP.get(t);
        if (clazz == null) {
            return false;
        }
        return clazz.isAssignableFrom(behavior.getClass());
    }

    private BlockFallbackConstants() {}
}
