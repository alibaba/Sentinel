package com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.degrade;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;

/**
 * @author cdfive
 */
public class QueryDegradeRuleListRespVo implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String app;

    private String ip;

    private Integer port;

    private String resource;

    private Integer grade;

    private Double count;

    private Integer timeWindow;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Integer getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Integer timeWindow) {
        this.timeWindow = timeWindow;
    }
}
