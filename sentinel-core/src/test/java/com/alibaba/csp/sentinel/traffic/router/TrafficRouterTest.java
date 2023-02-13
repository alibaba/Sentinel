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
package com.alibaba.csp.sentinel.traffic.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.traffic.ClusterManager;
import com.alibaba.csp.sentinel.traffic.DefaultInstanceManager;
import com.alibaba.csp.sentinel.traffic.Instance;
import com.alibaba.csp.sentinel.traffic.InstanceManager;
import com.alibaba.csp.sentinel.traffic.RouterFilter;
import com.alibaba.csp.sentinel.traffic.TrafficContext;
import com.alibaba.csp.sentinel.traffic.TrafficRouterFilter;
import com.alibaba.csp.sentinel.traffic.rule.TrafficRoutingRuleGroup;
import com.alibaba.csp.sentinel.traffic.rule.router.Route;
import com.alibaba.csp.sentinel.traffic.rule.router.RouteDetail;
import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouter;
import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouterRuleManager;
import com.alibaba.csp.sentinel.traffic.rule.router.destination.Destination;
import com.alibaba.csp.sentinel.traffic.rule.router.destination.RouteDestination;
import com.alibaba.csp.sentinel.traffic.rule.router.match.MethodMatch;
import com.alibaba.csp.sentinel.traffic.rule.router.match.RequestMatch;
import com.alibaba.csp.sentinel.traffic.rule.router.match.StringMatch;
import com.alibaba.csp.sentinel.traffic.rule.workload.Subset;
import com.alibaba.csp.sentinel.traffic.rule.workload.VirtualWorkload;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author panxiaojun233
 */
public class TrafficRouterTest {

	@Before
	public void before() {
		TrafficRoutingRuleGroup trafficRoutingRuleGroup = new TrafficRoutingRuleGroup();

		trafficRoutingRuleGroup.setAppName("test-consumer");
		List<TrafficRouter> trafficRouterRuleList = new LinkedList<>();
		TrafficRouter trafficRouterRule = new TrafficRouter();

		List<Route> http = new ArrayList<>();
		Route route = new Route();
		route.setName("test-traffic-provider-rule");

		List<RouteDetail> routeDetailList = new ArrayList<>();
		RouteDetail routeDetail = new RouteDetail();

		List<RequestMatch> requestMatches = new ArrayList<>();
		RequestMatch requestMatch = new RequestMatch();
		requestMatches.add(requestMatch);

		List<RouteDestination> routeDestinations = new ArrayList<>();
		RouteDestination routeDestination = new RouteDestination();
		routeDestinations.add(routeDestination);
		Destination destination = new Destination();
		destination.setSubset("v1");
		destination.setHost("test-provider");

		routeDestination.setDestination(destination);
		routeDetail.setMatch(requestMatches);
		routeDetail.setRoute(routeDestinations);


		MethodMatch methodMatch = new MethodMatch();
		methodMatch.setName(new StringMatch("hello", null,null));
		Map<String, StringMatch> headers = new HashMap<>();
		headers.put("x-tag", new StringMatch("v1", null,null));
		methodMatch.setHeaders(headers);
		requestMatch.setMethod(methodMatch);

		routeDetailList.add(routeDetail);
		route.setRouteDetail(routeDetailList);

		http.add(route);
		trafficRouterRule.setHttp(http);

		List<String> hosts = new ArrayList<>();
		hosts.add("test-provider");

		trafficRouterRule.setHosts(hosts);
		trafficRouterRuleList.add(trafficRouterRule);
		trafficRoutingRuleGroup.setTrafficRouterRuleList(trafficRouterRuleList);

		List<VirtualWorkload> virtualWorkloadRuleList = new ArrayList<>();
		VirtualWorkload virtualWorkload = new VirtualWorkload();
		virtualWorkload.setHost("test-provider");

		List<Subset> subsets = new ArrayList<>();
		Subset subset = new Subset();
		subset.setName("v1");
		Map<String, String> labels1 = new HashMap<>();
		labels1.put("instance-tag", "v1");
		subset.setLabels(labels1);
		subsets.add(subset);

		Subset subset2 = new Subset();
		subset2.setName("v2");
		Map<String, String> labels2 = new HashMap<>();
		labels2.put("instance-tag", "v2");
		subset2.setLabels(labels2);
		subsets.add(subset2);


		Subset subset3 = new Subset();
		subset3.setName("v3");
		Map<String, String> labels3 = new HashMap<>();
		labels3.put("instance-tag", "v3");
		subset3.setLabels(labels3);
		subsets.add(subset3);
		virtualWorkload.setSubsets(subsets);

		virtualWorkloadRuleList.add(virtualWorkload);
		trafficRoutingRuleGroup.setVirtualWorkloadRuleList(virtualWorkloadRuleList);

		TrafficRouterRuleManager.updateTrafficRouter(trafficRoutingRuleGroup);
	}

	@After
	public void after() {
		TrafficRouterRuleManager.updateTrafficRouter(new TrafficRoutingRuleGroup());
	}

	@Test
	public void testRoute() {
		List<RouterFilter>  filters = new ArrayList<>();
		InstanceManager instanceManager = new DefaultInstanceManager();
		ClusterManager clusterManager = new ClusterManager(filters,null, instanceManager);
		filters.add(new TrafficRouterFilter(clusterManager));
		TrafficRouterFilter trafficRouterFilter = new TrafficRouterFilter(clusterManager);

		List<Instance> instanceList = new ArrayList<>();

		Instance instance = new Instance();
		instance.setAppName("test-provider");
		instance.setHost("127.0.0.1");
		instance.setPort(80081);
		Map<String, String> meta1 = new HashMap<>();
		meta1.put("instance-tag" ,"v1");
		instance.setMetadata(meta1);
		instanceList.add(instance);

		Instance instance1 = new Instance();
		instance1.setAppName("test-provider");
		instance1.setHost("127.0.0.1");
		instance1.setPort(80082);
		Map<String, String> meta2 = new HashMap<>();
		meta2.put("instance-tag" ,"v2");
		instance1.setMetadata(meta2);
		instanceList.add(instance1);

		Instance instance2 = new Instance();
		instance2.setAppName("test-provider");
		instance2.setHost("127.0.0.1");
		instance2.setPort(80083);
		Map<String, String> meta3 = new HashMap<>();
		meta3.put("instance-tag" ,"v3");
		instance2.setMetadata(meta3);
		instanceList.add(instance2);

		TrafficContext context = new TrafficContext();
		Map<String, String> header = new HashMap<>();
		header.put("x-tag", "v1");
		context.setHeaders(header);
		context.setMethodName("hello");

		Assert.assertEquals(trafficRouterFilter.filter(instanceList,context).size(), 1);

	}
}
