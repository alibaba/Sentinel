package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.Date;


public class AuthorityRuleCorrectEntity implements RuleEntity {

    private Long id;
    private String app;
    private String ip;
    private Integer port;
    private String limitApp;
    private String resource;
    private Date gmtCreate;
    private Date gmtModified;

    private int strategy = 0;

    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    @Override
    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }

    @Override
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    @Override
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getLimitApp() { return limitApp; }
    public void setLimitApp(String limitApp) { this.limitApp = limitApp; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    @Override
    public Date getGmtCreate() { return gmtCreate; }
    public void setGmtCreate(Date gmtCreate) { this.gmtCreate = gmtCreate; }

    public Date getGmtModified() { return gmtModified; }
    public void setGmtModified(Date gmtModified) { this.gmtModified = gmtModified; }

    public int getStrategy() { return strategy; }
    public void setStrategy(int strategy) { this.strategy = strategy; }

    @Override
    public Rule toRule() {
        return null;
    }
}