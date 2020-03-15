package com.alibaba.csp.sentinel.cluster.redis.util;

public class ClientConstants {
    public static final String FLOW_CHECKER_TOKEN_KEY = "sentinel:token:";
    public static final String FLOW_RULE_CONFIG_KEY = "sentinel:config:";
    public static final Integer DEFAULT_MAX_WAIT_MILLIS = -1;
    public static final Integer DEFAULT_MAX_ACTIVE = 8;
    public static final Integer DEFAULT_MAX_IDLE = 8;
    public static final Integer DEFAULT_MIN_IDLE = 8;
    public static final Integer DEFAULT_CONNECT_TIMEOUT = 1000;
    public static final Integer DEFAULT_MAX_ATTEMPTS = 5;

    public static final String SAMPLE_COUNT_KEY = "sampleCount";
    public static final String INTERVAL_IN_MS_KEY = "intervalInMs";
    public static final String WINDOW_LENGTH_IN_MS_KEY = "windowLengthInMs";
    public static final String THRESHOLD_COUNT_KEY = "thresholdCount";
    public static final String FLOW_CHECKER_LUA = "flow_checker";

}
