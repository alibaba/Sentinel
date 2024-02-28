/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.webflow.rule;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;
import com.alibaba.csp.sentinel.webflow.SentinelWebFlowConstants;

/**
 * @author guanyu
 * @since 1.10.0
 */
public class WebFlowRule {

    private Long id;

    private String resource;
    private int resourceMode = SentinelWebFlowConstants.RESOURCE_MODE_INTERFACE_ID;

    private int grade = RuleConstant.FLOW_GRADE_QPS;
    private Double count;
    private long intervalMs = 1000;

    private int controlBehavior = RuleConstant.CONTROL_BEHAVIOR_DEFAULT;
    private int burst;
    /**
     * For throttle (rate limiting with queueing).
     */
    private int maxQueueingTimeoutMs = 500;

    /**
     * For parameter flow control. If not set, the web flow rule will be
     * converted to normal flow rule.
     */
    private WebParamItem paramItem;

    /**
     * Indicating whether the rule is for cluster mode.
     */
    private boolean clusterMode = false;
    /**
     * Cluster mode specific config for parameter flow rule.
     */
    private ParamFlowClusterConfig clusterConfig;

    public WebFlowRule() {}

    public WebFlowRule(String resource) {
        this.resource = resource;
    }

    public Long getId() {
        return id;
    }

    public WebFlowRule setId(Long id) {
        this.id = id;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public WebFlowRule setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public int getResourceMode() {
        return resourceMode;
    }

    public WebFlowRule setResourceMode(int resourceMode) {
        this.resourceMode = resourceMode;
        return this;
    }

    public int getControlBehavior() {
        return controlBehavior;
    }

    public WebFlowRule setControlBehavior(int controlBehavior) {
        this.controlBehavior = controlBehavior;
        return this;
    }

    public Double getCount() {
        return count;
    }

    public WebFlowRule setCount(Double count) {
        this.count = count;
        return this;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public WebFlowRule setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
        return this;
    }

    public WebParamItem getParamItem() {
        return paramItem;
    }

    public WebFlowRule setParamItem(WebParamItem paramItem) {
        this.paramItem = paramItem;
        return this;
    }

    public int getMaxQueueingTimeoutMs() {
        return maxQueueingTimeoutMs;
    }

    public WebFlowRule setMaxQueueingTimeoutMs(int maxQueueingTimeoutMs) {
        this.maxQueueingTimeoutMs = maxQueueingTimeoutMs;
        return this;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getBurst() {
        return burst;
    }

    public WebFlowRule setBurst(int burst) {
        this.burst = burst;
        return this;
    }

    public boolean isClusterMode() {
        return clusterMode;
    }

    public void setClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
    }

    public ParamFlowClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ParamFlowClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        WebFlowRule that = (WebFlowRule) o;

        if (resourceMode != that.resourceMode) { return false; }
        if (grade != that.grade) { return false; }
        if (Double.compare(that.count, count) != 0) { return false; }
        if (intervalMs != that.intervalMs) { return false; }
        if (controlBehavior != that.controlBehavior) { return false; }
        if (burst != that.burst) { return false; }
        if (maxQueueingTimeoutMs != that.maxQueueingTimeoutMs) { return false; }
        if (resource != null ? !resource.equals(that.resource) : that.resource != null) { return false; }
        return paramItem != null ? paramItem.equals(that.paramItem) : that.paramItem == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = resource != null ? resource.hashCode() : 0;
        result = 31 * result + resourceMode;
        result = 31 * result + grade;
        temp = Double.doubleToLongBits(count);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (intervalMs ^ (intervalMs >>> 32));
        result = 31 * result + controlBehavior;
        result = 31 * result + burst;
        result = 31 * result + maxQueueingTimeoutMs;
        result = 31 * result + (paramItem != null ? paramItem.hashCode() : 0);
        result = 31 * result + (clusterMode ? 1 : 0);
        result = 31 * result + (clusterConfig != null ? clusterConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WebFlowRule{" +
                "resource='" + resource + '\'' +
                ", resourceMode=" + resourceMode +
                ", grade=" + grade +
                ", count=" + count +
                ", intervalMs=" + intervalMs +
                ", controlBehavior=" + controlBehavior +
                ", burst=" + burst +
                ", maxQueueingTimeoutMs=" + maxQueueingTimeoutMs +
                ", paramItem=" + paramItem +
                ", clusterMode=" + clusterMode +
                ", clusterConfig=" + clusterConfig +
                '}';
    }
}
