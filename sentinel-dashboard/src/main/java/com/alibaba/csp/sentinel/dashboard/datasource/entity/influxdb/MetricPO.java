package com.alibaba.csp.sentinel.dashboard.datasource.entity.influxdb;

import java.time.Instant;
import org.influxdata.annotations.Column;
import org.influxdata.annotations.Measurement;

@Measurement(name = "sentinel_metric")
public class MetricPO {

    @Column(name = "time")
    private Instant time;

    @Column(name = "id")
    private Long id;

    @Column(name = "gmtCreate")
    private Long gmtCreate;

    @Column(name = "gmtModified")
    private Long gmtModified;

    @Column(name = "app", tag = true)
    private String app;

    @Column(name = "resource", tag = true)
    private String resource;

    @Column(name = "passQps")
    private Long passQps;

    @Column(name = "successQps")
    private Long successQps;

    @Column(name = "blockQps")
    private Long blockQps;

    @Column(name = "exceptionQps")
    private Long exceptionQps;

    @Column(name = "rt")
    private double rt;

    @Column(name = "count")
    private int count;

    @Column(name = "resourceCode")
    private int resourceCode;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Long getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(int resourceCode) {
        this.resourceCode = resourceCode;
    }
}