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
package com.alibaba.csp.sentinel.traffic.rule;

import java.util.LinkedList;
import java.util.List;

import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouter;
import com.alibaba.csp.sentinel.traffic.rule.workload.VirtualWorkload;

/**
 * @author panxiaojun233
 */
public class TrafficRoutingRuleGroup {
    private String appName;
    private List<TrafficRouter> trafficRouterRuleList = new LinkedList<>();
    private List<VirtualWorkload> virtualWorkloadRuleList = new LinkedList<>();

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<TrafficRouter> getTrafficRouterRuleList() {
        return trafficRouterRuleList;
    }

    public void setTrafficRouterRuleList(List<TrafficRouter> trafficRouterRuleList) {
        this.trafficRouterRuleList = trafficRouterRuleList;
    }

    public List<VirtualWorkload> getVirtualWorkloadRuleList() {
        return virtualWorkloadRuleList;
    }

    public void setVirtualWorkloadRuleList(List<VirtualWorkload> virtualWorkloadRuleList) {
        this.virtualWorkloadRuleList = virtualWorkloadRuleList;
    }

    public boolean isValid() {
        return trafficRouterRuleList.size() > 0 && virtualWorkloadRuleList.size() > 0;
    }

    @Override
    public String toString() {
        return "TrafficRoutingRuleGroup{" +
                "appName='" + appName + '\'' +
                ", trafficRouterRuleList=" + trafficRouterRuleList +
                ", virtualWorkloadRuleList=" + virtualWorkloadRuleList +
                '}';
    }
}
