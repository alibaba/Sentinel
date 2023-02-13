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
package com.alibaba.csp.sentinel.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.traffic.TrafficRouterFilter;
import com.alibaba.csp.sentinel.traffic.rule.TrafficRoutingRuleGroup;
import com.alibaba.csp.sentinel.traffic.rule.router.RouteDetail;
import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouter;
import com.alibaba.csp.sentinel.traffic.rule.router.destination.Destination;
import com.alibaba.csp.sentinel.traffic.rule.router.destination.RouteDestination;
import com.alibaba.csp.sentinel.traffic.rule.router.match.MethodMatch;
import com.alibaba.csp.sentinel.traffic.rule.router.match.RequestMatch;
import com.alibaba.csp.sentinel.traffic.rule.router.match.StringMatch;
import com.alibaba.csp.sentinel.traffic.rule.workload.Subset;
import com.alibaba.csp.sentinel.traffic.rule.workload.VirtualWorkload;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;

/**
 * The rule parser for OpenSergo TrafficRouter.
 *
 * @author panxiaojun233
 */
public class OpenSergoTrafficRouterParser {

    private final static String SUBSET_KEY = "subset";

    public OpenSergoTrafficRouterParser() {
    }

    /**
     * Parse RDS's {@link RouteConfiguration} to Sentinel2.0's {@link TrafficRoutingRuleGroup}
     *
     * @param routeConfigurations the Rule from OpenSergo control plane.
     * @return {@link TrafficRoutingRuleGroup} the traffic rule for {@link TrafficRouterFilter}.
     * @throws InvalidProtocolBufferException
     */
    public TrafficRoutingRuleGroup resolveLabelRouting(List<RouteConfiguration> routeConfigurations) throws InvalidProtocolBufferException {

        TrafficRoutingRuleGroup trafficRoutingRuleGroup = new TrafficRoutingRuleGroup();

        List<TrafficRouter> trafficRouters = new ArrayList<>();
        trafficRoutingRuleGroup.setTrafficRouterRuleList(trafficRouters);

        List<VirtualWorkload> workloads = new ArrayList<>();
        trafficRoutingRuleGroup.setVirtualWorkloadRuleList(workloads);

        for (RouteConfiguration routeConfiguration : routeConfigurations) {
            List<VirtualHost> virtualHosts = routeConfiguration.getVirtualHostsList();
            for (VirtualHost virtualHost : virtualHosts) {
                TrafficRouter trafficRouter = new TrafficRouter();
                trafficRouters.add(trafficRouter);
                //todo 怎么知道是rpc/http?
                String remoteAppName = "";
                String[] serviceAndPort = virtualHost.getName().split(":");
                if (serviceAndPort.length > 0) {
                    remoteAppName = serviceAndPort[0].split("\\.")[0];
                }
                List<String> hosts = new ArrayList<>();
                hosts.add(remoteAppName);
                trafficRouter.setHosts(hosts);

                List<Route> routes = virtualHost.getRoutesList();
                List<com.alibaba.csp.sentinel.traffic.rule.router.Route> http = new ArrayList<>();
                trafficRouter.setHttp(http);

                // todo fallback
                for (Route route : routes) {
                    com.alibaba.csp.sentinel.traffic.rule.router.Route routeRule = new com.alibaba.csp.sentinel.traffic.rule.router.Route();
                    http.add(routeRule);

                    StringMatch cluster = new StringMatch();
                    cluster.setExact(remoteAppName);

                    List<RouteDetail> routeDetails = new ArrayList<>();
                    routeRule.setRouteDetail(routeDetails);

                    RouteDetail routeDetail = new RouteDetail();
                    routeDetails.add(routeDetail);

                    List<RequestMatch> match = new ArrayList<>();
                    routeDetail.setMatch(match);

                    List<RequestMatch> requestMatches = match2RouteRules(route.getMatch());
                    routeDetail.setMatch(requestMatches);

                    List<RouteDestination> routeDestinations = new ArrayList<>();
                    routeDetail.setRoute(routeDestinations);

                    RouteDestination routeDestination = new RouteDestination();
                    routeDestinations.add(routeDestination);

                    Destination destination = new Destination();
                    routeDestination.setDestination(destination);

                    destination.setHost(remoteAppName);
                    destination.setSubset(getSubset(route, route.getRoute().getCluster()));

                    VirtualWorkload virtualWorkload = new VirtualWorkload();
                    virtualWorkload.setHost(remoteAppName);

                    List<Subset> subsets = new ArrayList<>();
                    Subset subset = new Subset();
                    subsets.add(subset);

                    subset.setName(getSubset(route, route.getRoute().getCluster()));
                    Map<String, String> labels = new HashMap<String, String>();
                    labels.put(SUBSET_KEY, subset.getName());
                    subset.setLabels(labels);

                    virtualWorkload.setSubsets(subsets);
                    workloads.add(virtualWorkload);
                }
            }
        }

        return trafficRoutingRuleGroup;
    }

    private RequestMatch headerMatcher2HeaderRule(HeaderMatcher headerMatcher) {
        StringMatch stringMatcher = Utils
                .convStringMatcher(Utils.headerMatch2StringMatch(headerMatcher));
        if (stringMatcher != null) {
            RequestMatch requestMatch = new RequestMatch();
            MethodMatch method = new MethodMatch();
            requestMatch.setMethod(method);

            Map<String, StringMatch> headers = new HashMap<>();
            headers.put(headerMatcher.getName(), stringMatcher);
            method.setHeaders(headers);

            return requestMatch;
        }
        return null;
    }


    private List<RequestMatch> match2RouteRules(RouteMatch routeMatch) {
        List<RequestMatch> requestMatches = new ArrayList<>();
        for (HeaderMatcher headerMatcher : routeMatch.getHeadersList()) {
            RequestMatch requestMatch = headerMatcher2HeaderRule(headerMatcher);
            if (requestMatch != null) {
                requestMatches.add(requestMatch);
            }
        }

        return requestMatches;
    }

    private String getSubset(Route route, String cluster) {
        String version = "";
        try {
            String[] info = cluster.split("\\|");
            version = info[2];
        } catch (Exception e) {
            RecordLog.error("invalid cluster info for route {}", route.getName());
        }
        return version;
    }
}
