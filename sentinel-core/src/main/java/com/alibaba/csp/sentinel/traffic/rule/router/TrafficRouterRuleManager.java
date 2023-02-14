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
package com.alibaba.csp.sentinel.traffic.rule.router;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.spi.SpiLoader;
import com.alibaba.csp.sentinel.traffic.RemoteAppObserver;
import com.alibaba.csp.sentinel.traffic.rule.TrafficRoutingRuleGroup;

/**
 * @author panxiaojun233
 */
public class TrafficRouterRuleManager {

    private static final ConcurrentHashMap<String, Object> trafficRouterRuleListenerMap = new ConcurrentHashMap<>();
    private static List<RemoteAppObserver> subscribers = SpiLoader.of(RemoteAppObserver.class)
            .loadInstanceList();
    private static volatile TrafficRoutingRuleGroup trafficRoutingRuleGroup = new TrafficRoutingRuleGroup();
    private static final Object object = new Object();

    public static synchronized void updateTrafficRouter(TrafficRoutingRuleGroup rules) {
        trafficRoutingRuleGroup = rules;
    }

    public static TrafficRoutingRuleGroup getTrafficRoutingRules() {
        return trafficRoutingRuleGroup;
    }

    public static synchronized void register(String app) {
        Object value = trafficRouterRuleListenerMap.put(app, object);
        if (value == null) {
            for (RemoteAppObserver subscriber : subscribers) {
                subscriber.onRemoteAppAppears(app);
            }
        }
    }

    public static synchronized void unregister(String app) {
        for (RemoteAppObserver subscriber : subscribers) {
            subscriber.onRemoteAppDisappears(app);
        }
        trafficRouterRuleListenerMap.remove(app);
    }
}
