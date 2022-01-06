package com.alibaba.csp.sentinel.dashboard.rule;

public interface DynamicRule<T>
{
    T getRules(String appName) throws Exception;
    
    void publish(String app, T rules) throws Exception;
}
