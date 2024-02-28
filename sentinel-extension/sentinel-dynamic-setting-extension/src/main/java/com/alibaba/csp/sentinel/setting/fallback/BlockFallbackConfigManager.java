package com.alibaba.csp.sentinel.setting.fallback;

import java.util.HashMap;
import java.util.Map;

public class BlockFallbackConfigManager {

    /**
     * (resource, (targetType, config))
     */
    private volatile Map<String, Map<Integer, BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior>>> webFallbackConfigMap
            = new HashMap<String, Map<Integer, BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior>>>();

    private static class InstanceHolder {
        private static final BlockFallbackConfigManager INSTANCE = new BlockFallbackConfigManager();
    }

    public static BlockFallbackConfigManager getInstance() {
        return BlockFallbackConfigManager.InstanceHolder.INSTANCE;
    }

    public BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior> getWebFallbackConfig(String resource,
                                                                                                  Class<?> exceptionClazz) {
        if (resource == null || exceptionClazz == null) {
            return null;
        }
        Map<Integer, BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior>> m = webFallbackConfigMap.get(resource);
        if (m == null || m.isEmpty()) {
            return null;
        }
        Integer blockType = BlockFallbackConstants.parseBlockType(exceptionClazz);
        // Unknown block type: we can try the block fallback for all types (if present).
        if (blockType == null) {
            return m.get(BlockFallbackConstants.BLOCK_TYPE_ALL);
        }
        BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior> c = m.get(blockType);
        if (c == null) {
            c = m.get(BlockFallbackConstants.BLOCK_TYPE_ALL);
        }
        return c;
    }
}
