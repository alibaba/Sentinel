package com.alibaba.csp.sentinel.setting.adapter;

/**
 * @author Eric Zhao
 */
public class AdapterSettingManager {
    private static volatile SentinelAdapterSettingEntity currentSetting = null;

    public static String getWebRespMessage() {
        return currentSetting == null ? null : currentSetting.getWebRespMessage();
    }

    public static Integer getWebRespContentType() {
        return currentSetting == null ? null : currentSetting.getWebRespContentType();
    }

    private AdapterSettingManager() {}
}
