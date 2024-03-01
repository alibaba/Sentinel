package com.alibaba.csp.sentinel.setting.fallback;

import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.8.8
 */
public class BlockFallbackConfigManager {

    /**
     * (resource, (targetType, config))
     */
    private volatile Map<String, Map<Integer, BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior>>> webFallbackConfigMap
            = new HashMap<String, Map<Integer, BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior>>>();

    private SentinelProperty<List<BlockFallbackConfig<Object>>> currentProperty
            = new DynamicSentinelProperty<List<BlockFallbackConfig<Object>>>();

    private static class InstanceHolder {
        private static final BlockFallbackConfigManager INSTANCE = new BlockFallbackConfigManager();
    }

    public static BlockFallbackConfigManager getInstance() {
        return BlockFallbackConfigManager.InstanceHolder.INSTANCE;
    }

    /**
     * Load new block fallback config, which will replace existing config.
     *
     * @param configList new config list to load
     * @return whether the config was actually updated
     */
    public boolean loadConfig(List<BlockFallbackConfig<Object>> configList) {
        return currentProperty.updateValue(configList);
    }

    public BlockFallbackConfig<BlockFallbackConfig.WebBlockFallbackBehavior> getWebFallbackConfig(
            String resource,
            Class<?> exceptionClazz
    ) {
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
