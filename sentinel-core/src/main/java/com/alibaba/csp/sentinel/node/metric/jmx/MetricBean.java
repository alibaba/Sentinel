package com.alibaba.csp.sentinel.node.metric.jmx;

import com.alibaba.csp.sentinel.node.metric.MetricNode;

/**
 * @author chenglu
 */
public class MetricBean implements MetricMXBean {
    
    private String resource;
    /**
     * Resource classification (e.g. SQL or RPC)
     * @since 1.7.0
     */
    private int classification;
    
    private long timestamp;
    private long passQps;
    private long blockQps;
    private long successQps;
    private long exceptionQps;
    private long rt;
    
    /**
     * @since 1.5.0
     */
    private long occupiedPassQps;
    /**
     * @since 1.7.0
     */
    private int concurrency;
    
    @Override
    public String getResource() {
        return resource;
    }
    
    @Override
    public int getClassification() {
        return classification;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public long getPassQps() {
        return passQps;
    }
    
    @Override
    public long getBlockQps() {
        return blockQps;
    }
    
    @Override
    public long getSuccessQps() {
        return successQps;
    }
    
    @Override
    public long getExceptionQps() {
        return exceptionQps;
    }
    
    @Override
    public long getRt() {
        return rt;
    }
    
    @Override
    public long getOccupiedPassQps() {
        return occupiedPassQps;
    }
    
    @Override
    public int getConcurrency() {
        return concurrency;
    }
    
    /**
     * reset the MBean value to the initialized value
     */
    public void reset() {
        this.blockQps = 0;
        this.passQps = 0;
        this.timestamp = System.currentTimeMillis();
        this.exceptionQps = 0;
        this.occupiedPassQps = 0;
        this.successQps = 0;
    }
    
    /**
     *
     * @param metricNode metric Node for write file
     */
    public void setValueFromNode(MetricNode metricNode) {
        if (metricNode == null) {
            return;
        }
        this.successQps = metricNode.getSuccessQps();
        this.blockQps = metricNode.getBlockQps();
        this.passQps = metricNode.getPassQps();
        this.occupiedPassQps = metricNode.getOccupiedPassQps();
        this.exceptionQps = metricNode.getExceptionQps();
        this.timestamp = metricNode.getTimestamp();
        this.classification = metricNode.getClassification();
        this.concurrency = metricNode.getConcurrency();
        this.resource = metricNode.getResource();
        this.rt = metricNode.getRt();
    }
}
