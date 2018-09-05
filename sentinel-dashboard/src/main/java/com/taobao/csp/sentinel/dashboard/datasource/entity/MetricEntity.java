/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.csp.sentinel.dashboard.datasource.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * @author leyouqueryTopResourceMetric
 * @author huyong
 */
@Getter
@Setter
public class MetricEntity implements Serializable {

    private static final long serialVersionUID = 6844720487195253513L;

    private Long id;
    private Date gmtCreate;
    private Date gmtModified;
    private String app;
    /**
     * 监控信息的时间戳
     */
    private Date timestamp;
    private String resource;
    private Long passedQps;
    private Long successQps;
    private Long blockedQps;
    /**
     * 发生异常的次数
     */
    private Long exception;

    /**
     * 所有successQps的Rt的和。
     */
    private double rt;

    /**
     * 本次聚合的总条数
     */
    private int count;

    private int resourceCode;

    public MetricEntity() {
    }

    public static MetricEntity copyOf(MetricEntity oldEntity) {
        MetricEntity entity = new MetricEntity();
        entity.setId(oldEntity.getId());
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(oldEntity.getGmtModified());
        entity.setApp(oldEntity.getApp());
        entity.setTimestamp(oldEntity.getTimestamp());
        entity.setResource(oldEntity.getResource());
        entity.setPassedQps(oldEntity.getPassedQps());
        entity.setBlockedQps(oldEntity.getBlockedQps());
        entity.setSuccessQps(oldEntity.getSuccessQps());
        entity.setException(oldEntity.getException());
        entity.setRt(oldEntity.getRt());
        entity.setCount(oldEntity.getCount());
        entity.setResource(oldEntity.getResource());
        return entity;
    }

    public synchronized void addPassedQps(Long passedQps) {
        this.passedQps += passedQps;
    }

    public synchronized void addBlockedQps(Long blockedQps) {
        this.blockedQps += blockedQps;
    }

    public synchronized void addException(Long exception) {
        this.exception += exception;
    }

    public synchronized void addCount(int count) {
        this.count += count;
    }

    public synchronized void addRtAndSuccessQps(double avgRt, Long successQps) {
        this.rt += avgRt * successQps;
        this.successQps += successQps;
    }

    /**
     * {@link #rt} = {@code avgRt * successQps}
     *
     * @param avgRt average rt of {@code successQps}
     */
    public synchronized void setRtAndSuccessQps(double avgRt, Long successQps) {
        this.rt = avgRt * successQps;
        this.successQps = successQps;
    }

    public void setResource(String resource) {
        this.resource = resource;
        this.resourceCode = resource.hashCode();
    }

    @Override
    public String toString() {
        return "MetricEntity{" +
                "id=" + id +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", app='" + app + '\'' +
                ", timestamp=" + timestamp +
                ", resource='" + resource + '\'' +
                ", passedQps=" + passedQps +
                ", blockedQps=" + blockedQps +
                ", successQps=" + successQps +
                ", exception=" + exception +
                ", rt=" + rt +
                ", count=" + count +
                ", resourceCode=" + resourceCode +
                '}';
    }

}
