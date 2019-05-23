package com.alibaba.csp.sentinel.dashboard.datasource.entity.jpa;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author cdfive
 * @date 2018-09-14
 */
@Entity
@Table(name = "sentinel_metric")
public class MetricPO implements Serializable {

    private static final long serialVersionUID = 7200023615444172715L;

    /** id，主键 */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    /** 创建时间 */
    @Column(name = "gmt_create")
    private Date gmtCreate;

    /** 修改时间 */
    @Column(name = "gmt_modified")
    private Date gmtModified;

    /** 应用名称 */
    @Column(name = "app")
    private String app;

    /** 统计时间 */
    @Column(name = "timestamp")
    private Date timestamp;

    /** 资源名称 */
    @Column(name = "resource")
    private String resource;

    /** 通过qps */
    @Column(name = "pass_qps")
    private Long passQps;

    /** 成功qps */
    @Column(name = "success_qps")
    private Long successQps;

    /** 限流qps */
    @Column(name = "block_qps")
    private Long blockQps;

    /** 发送异常的次数 */
    @Column(name = "exception_qps")
    private Long exceptionQps;

    /** 所有successQps的rt的和 */
    @Column(name = "rt")
    private Double rt;

    /** 本次聚合的总条数 */
    @Column(name = "_count")
    private Integer count;

    /** 资源的hashCode */
    @Column(name = "resource_code")
    private Integer resourceCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    public Double getRt() {
        return rt;
    }

    public void setRt(Double rt) {
        this.rt = rt;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(Integer resourceCode) {
        this.resourceCode = resourceCode;
    }
}