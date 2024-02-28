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

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

/**
 * @author Eric Zhao
 * @author guanyu
 * @since 1.8.2
 */
public final class BlockFallbackUtils {

    public static BlockFallbackConfig.WebBlockFallbackBehavior getFallbackBehavior(String resource, BlockException ex) {
        if (ex == null) {
            return null;
        }
        BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior> c;
        // 系统保护规则特殊处理，取这个约定好的名称
        if (ex instanceof SystemBlockException) {
            c = BlockFallbackConfigManager.getInstance().getWebFallbackConfig(BlockFallbackConstants.SYSTEM_RESOURCE,
                    ex.getClass());
        } else {
            c = BlockFallbackConfigManager.getInstance().getWebFallbackConfig(resource, ex.getClass());
        }
        if (c == null) {
            return null;
        }
        return c.getFallbackBehavior();
    }

    private BlockFallbackUtils() {}
}
