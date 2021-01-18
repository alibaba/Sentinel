package com.alibaba.csp.sentinel.node.metric.jmx;

/**
 * the Metric JMX Bean interface
 * @author chenglu
 */
public interface MetricMXBean {
    
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
