package com.alibaba.csp.sentinel.dashboard.rule;

public interface RuleConfigService {
    String getConfig(String dataId, String group);
    void publishConfig(String dataId,String group,String content);
}
