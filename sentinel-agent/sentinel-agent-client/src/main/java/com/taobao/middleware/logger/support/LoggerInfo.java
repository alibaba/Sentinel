package com.taobao.middleware.logger.support;

import java.util.HashMap;
import java.util.List;

/**
 * @author zhuyong on 2017/06/29
 */
public class LoggerInfo extends HashMap {

    private static String level = "level";
    private static String effectiveLevel = "effectiveLevel";
    private static String additivity = "additivity";
    private static String appenders = "appenders";

    public LoggerInfo(String name, boolean additivity) {
        put(LoggerInfo.additivity, additivity);
    }

    public void setLevel(String level) {
        put(LoggerInfo.level, level);
    }

    public void setEffectiveLevel(String effectiveLevel) {
        put(LoggerInfo.effectiveLevel, effectiveLevel);
    }

    public String getLevel() {
        return (String) get(level);
    }

    public List<AppenderInfo> getAppenders() {
        return (List<AppenderInfo>) get(appenders);
    }

    public void setAppenders(List<AppenderInfo> appenders) {
        put(LoggerInfo.appenders, appenders);
    }
}
