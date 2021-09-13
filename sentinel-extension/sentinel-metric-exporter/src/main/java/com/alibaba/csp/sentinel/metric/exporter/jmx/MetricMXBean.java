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

/**
 * the Metric JMX Bean interface.
 *
 * @author chenglu
 * @date 2021-07-01 20:02
 * @since 1.8.3
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

    long getVersion();
}
