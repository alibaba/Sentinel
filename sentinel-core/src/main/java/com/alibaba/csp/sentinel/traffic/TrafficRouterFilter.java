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
package com.alibaba.csp.sentinel.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.alibaba.csp.sentinel.traffic.rule.TrafficRoutingRuleGroup;
import com.alibaba.csp.sentinel.traffic.rule.router.Route;
import com.alibaba.csp.sentinel.traffic.rule.router.RouteDetail;
import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouter;
import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouterRuleManager;
import com.alibaba.csp.sentinel.traffic.rule.router.destination.Destination;
import com.alibaba.csp.sentinel.traffic.rule.router.destination.RouteDestination;
import com.alibaba.csp.sentinel.traffic.rule.router.match.RequestMatch;
import com.alibaba.csp.sentinel.traffic.rule.router.match.StringMatch;
import com.alibaba.csp.sentinel.traffic.rule.workload.Subset;
import com.alibaba.csp.sentinel.traffic.rule.workload.VirtualWorkload;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author panxiaojun233
 */
public class TrafficRouterFilter implements RouterFilter {

    private final ClusterManager clusterManager;

    public TrafficRouterFilter(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public List<Instance> filter(List<Instance> instanceList, TrafficContext context) throws TrafficException {
        if (null == instanceList || instanceList.size() == 0) {
            return instanceList;
        }
        TrafficRoutingRuleGroup routingRules = TrafficRouterRuleManager.getTrafficRoutingRules();
        if (!routingRules.isValid()) {
            return instanceList;
        }
        List<RouteDestination> routeDestination = getRouteDestination(routingRules, context);
        List<Instance> targetInstances = new ArrayList<>();
        if (routeDestination != null) {
            String appName = instanceList.get(0).getAppName();
            // aggregate target invokers
            String subset = randomSelectDestination(appName, routeDestination, instanceList);
            if (subset != null) {
                List<Instance> destination = getSubsetInstances(appName, subset, instanceList);
                targetInstances.addAll(destination);
            }
        }
        return targetInstances;
    }

    protected String randomSelectDestination(String appName, List<RouteDestination> routeDestination, List<Instance> instanceList) throws TrafficException {
        // randomly select one DubboRouteDestination from list by weight
        int totalWeight = 0;
        for (RouteDestination dubboRouteDestination : routeDestination) {
            totalWeight += Math.max(dubboRouteDestination.getWeight(), 1);
        }
        int target = ThreadLocalRandom.current().nextInt(totalWeight);
        for (RouteDestination destination : routeDestination) {
            target -= Math.max(destination.getWeight(), 1);
            if (target <= 0) {
                // match weight
                String result = computeDestination(appName, destination.getDestination(), instanceList);
                if (result != null) {
                    return result;
                }
            }
        }

        // fall back
        for (RouteDestination destination : routeDestination) {
            String result = computeDestination(appName, destination.getDestination(), instanceList);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    protected String computeDestination(String appName, Destination destination, List<Instance> instanceList) throws TrafficException {
        String subset = destination.getSubset();

        do {
            List<Instance> result = getSubsetInstances(appName, subset, instanceList);

            List<Instance> newResult = new ArrayList<>(instanceList);
            newResult.addAll(result);
            if (!result.isEmpty() && !newResult.isEmpty()) {
                return subset;
            }

            // fall back
            RouteDestination routeDestination = destination.getFallback();
            if (routeDestination == null) {
                break;
            }
            destination = routeDestination.getDestination();

            if (destination == null) {
                break;
            }
            subset = destination.getSubset();
        }
        while (true);

        return null;
    }

    //todo 地址通知时缓存优化
    private List<Instance> getSubsetInstances(String appName, String subset, List<Instance> instanceList) {
        TrafficRoutingRuleGroup rules = TrafficRouterRuleManager.getTrafficRoutingRules();
        List<Instance> results = new ArrayList<>();
        for (VirtualWorkload virtualWorkload : rules.getVirtualWorkloadRuleList()) {
            if (virtualWorkload.getHost().equals(appName)) {
                for (Subset subsetRule : virtualWorkload.getSubsets()) {
                    if (subsetRule.getName().equals(subset)) {
                        Map<String, String> labels = subsetRule.getLabels();
                        for (String labelKey : labels.keySet()) {
                            for (Instance instance : instanceList) {
                                String labelValue = instance.getMetadata().get(labelKey);
                                if (StringUtil.isNotEmpty(labelValue) && labelValue.equals(labels.get(labelKey))) {
                                    results.add(instance);
                                }
                            }
                        }
                    }
                }
            }
        }

        return results;
    }


    protected List<RouteDestination> getRouteDestination(TrafficRoutingRuleGroup trafficRoutingRuleGroup, TrafficContext context) {
        if (trafficRoutingRuleGroup != null) {
            List<TrafficRouter> trafficRouterRuleList = trafficRoutingRuleGroup.getTrafficRouterRuleList();
            if (trafficRouterRuleList != null && trafficRouterRuleList.size() > 0) {
                for (TrafficRouter trafficRouter : trafficRouterRuleList) {
                    // match virtual service (by host/serviceName/)
                    List<Route> rpcRoutes = getRoutes(trafficRouter, context);
                    if (rpcRoutes != null) {
                        // match route detail (by params)
                        return getRouteDestination(rpcRoutes, context);
                    }
                }
            }
        }
        return null;
    }

    protected List<RouteDestination> getRouteDestination(List<Route> routes, TrafficContext context) {

        for (Route route : routes) {
            List<RouteDetail> routeDetailList = route.getRouteDetail();
            if (routeDetailList != null && routeDetailList.size() > 0) {
                for (RouteDetail routeDetail : routeDetailList) {
                    List<RequestMatch> requestMatchList = routeDetail.getMatch();
                    if (requestMatchList == null || requestMatchList.isEmpty()) {
                        return routeDetail.getRoute();
                    }

                    if (requestMatchList.stream().allMatch(
                            request -> request.isMatch(context))) {
                        return routeDetail.getRoute();
                    }
                }
            }
        }

        return null;
    }

    //todo appName 匹配
    protected List<Route> getRoutes(TrafficRouter trafficRouter, TrafficContext context) {
        List<Route> rpcRouteList = trafficRouter.getHttp();
        List<Route> result = new ArrayList<>();
        if (rpcRouteList != null && rpcRouteList.size() > 0) {
            for (Route rpcRoute : rpcRouteList) {
                List<StringMatch> stringMatchList = rpcRoute.getServices();
                if (stringMatchList == null || stringMatchList.size() == 0) {
                    result.add(rpcRoute);
                    continue;
                }

                for (StringMatch stringMatch : stringMatchList) {
                    if (stringMatch.isMatch(context.getServiceName())) {
                        result.add(rpcRoute);
                    }
                }
            }
        }

        return result;
    }
}
