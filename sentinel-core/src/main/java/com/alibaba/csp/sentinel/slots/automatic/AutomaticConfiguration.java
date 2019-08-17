package com.alibaba.csp.sentinel.slots.automatic;

public class AutomaticConfiguration {

    /**
     * 系统 CPU useage 上限
     */
    public static float MAX_CPU_USEAGE = 80;

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
