package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;

import java.util.*;

public class ParamFlowRuleCorrectEntity implements RuleEntity {

    private Long id;
    private String app;
    private String ip;
    private Integer port;
    private String limitApp;
    private String resource;
    private Date gmtCreate;

    private int grade = 1;
    private Integer paramIdx = 0;
    private double count;
    private int controlBehavior = 0;
    private int maxQueueingTimeMs = 0;
    private int burstCount = 0;
    private long durationInSec = 1L;
    private List<ParamFlowItem> paramFlowItemList = new ArrayList<>();
    private Map<Object, Integer> hotItems = new HashMap<>();
    private boolean clusterMode = false;
    private ParamFlowClusterConfig clusterConfig;

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

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public Integer getParamIdx() { return paramIdx; }
    public void setParamIdx(Integer paramIdx) { this.paramIdx = paramIdx; }

    public double getCount() { return count; }
    public void setCount(double count) { this.count = count; }

    public int getControlBehavior() { return controlBehavior; }
    public void setControlBehavior(int controlBehavior) { this.controlBehavior = controlBehavior; }

    public int getMaxQueueingTimeMs() { return maxQueueingTimeMs; }
    public void setMaxQueueingTimeMs(int maxQueueingTimeMs) { this.maxQueueingTimeMs = maxQueueingTimeMs; }

    public int getBurstCount() { return burstCount; }
    public void setBurstCount(int burstCount) { this.burstCount = burstCount; }

    public long getDurationInSec() { return durationInSec; }
    public void setDurationInSec(long durationInSec) { this.durationInSec = durationInSec; }

    public List<ParamFlowItem> getParamFlowItemList() { return paramFlowItemList; }
    public void setParamFlowItemList(List<ParamFlowItem> paramFlowItemList) { this.paramFlowItemList = paramFlowItemList; }

    public Map<Object, Integer> getHotItems() { return hotItems; }
    public void setHotItems(Map<Object, Integer> hotItems) { this.hotItems = hotItems; }

    public boolean isClusterMode() { return clusterMode; }
    public void setClusterMode(boolean clusterMode) { this.clusterMode = clusterMode; }

    public ParamFlowClusterConfig getClusterConfig() { return clusterConfig; }
    public void setClusterConfig(ParamFlowClusterConfig clusterConfig) { this.clusterConfig = clusterConfig; }

    @Override
    public Rule toRule() {
        return null;
    }
}