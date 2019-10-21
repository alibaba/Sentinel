package com.alibaba.csp.ahas.sentinel.util;

import com.alibaba.csp.ahas.sentinel.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 */
public final class MachineUtils {

    private static String currentProcessConfigurationId = null;

    public static String getCurrentProcessConfigurationId() {
        return currentProcessConfigurationId;
    }

    public static void setCurrentProcessConfigurationId(String currentProcessConfigurationId) {
        MachineUtils.currentProcessConfigurationId = currentProcessConfigurationId;
    }

    public static boolean isCurrentMachineEqual(ClusterGroupEntity group) {
        if (StringUtil.isEmpty(currentProcessConfigurationId)) {
            return false;
        }
        return currentProcessConfigurationId.equals(group.getMachineId());
    }

    private MachineUtils() {}
}
