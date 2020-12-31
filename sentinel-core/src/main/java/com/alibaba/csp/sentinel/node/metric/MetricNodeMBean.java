package com.alibaba.csp.sentinel.node.metric;

public interface MetricNodeMBean {
    long getTimestamp();
    
    long getOccupiedPassQps();
    
    long getSuccessQps();
    
    long getPassQps();
    
    long getExceptionQps();
    
    long getBlockQps();
    
    long getRt();
    
    String getResource();
    
    int getClassification();
    
    int getConcurrency();
}
