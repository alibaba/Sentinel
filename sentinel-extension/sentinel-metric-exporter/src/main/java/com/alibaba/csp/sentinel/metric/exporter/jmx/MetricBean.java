/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.csp.sentinel.metric.exporter.jmx;

import com.alibaba.csp.sentinel.node.metric.MetricNode;

/**
 * the MetricBean for JMX expose.
 *
 * @author chenglu
 * @date 2021-07-01 20:02
 * @since 1.8.3
 */
public class MetricBean implements MetricMXBean {
    
    private String resource;
    
    /**
     * Resource classification (e.g. SQL or RPC)
     */
    private int classification;
    
    private long timestamp;
    
    private long passQps;
    
    private long blockQps;
    
    private long successQps;
    
    private long exceptionQps;
    
    private long rt;
    
    private long occupiedPassQps;

    private int concurrency;

    private long version;
    
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

    @Override
    public long getVersion() {
        return version;
    }

    /**
     * set the version to current Mbean.
     *
     * @param version current version.
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * reset the MBean value to the initialized value.
     */
    public void reset() {
        this.blockQps = 0;
        this.passQps = 0;
        this.timestamp = System.currentTimeMillis();
        this.exceptionQps = 0;
        this.occupiedPassQps = 0;
        this.successQps = 0;
        this.rt = 0;
        this.concurrency = 0;
    }
    
    /**
     * set the MetricBean's value which from MetricNode.
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
