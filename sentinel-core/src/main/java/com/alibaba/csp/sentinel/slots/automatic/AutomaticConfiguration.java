package com.alibaba.csp.sentinel.slots.automatic;

/**
 * Configuration for {AutomaticRuleManager}
 *
 * @author Li Yudong
 */
public class AutomaticConfiguration {


    /**
     * System Load 阈值
     */
    public static float MAX_SYSTEM_LOAD = 5;

    /**
     * 初始化流控阈值
     */
    public static double DEFAULT_COUNT = 125;

    /**
     * 规则更新频率（ms)
     */
    public static long RULE_UPDATE_WINDOW = 1000;

    /**
     * 资源流控阈值最小值
     */
    public static int RESOURCE_MIN_FLOW = 10;

    public static double DEGRADE_RT = 4900;

    public static int DEGRADE_TIME_WINDOW = 5;

}
