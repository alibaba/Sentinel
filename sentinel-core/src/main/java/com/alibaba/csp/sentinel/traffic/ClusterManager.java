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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouterRuleManager;

/**
 * @author panxiaojun233
 */
public class ClusterManager implements InstanceListener {
    private List<RouterFilter> routerFilterList;
    private LoadBalancer loadBalancer;
    private InstanceManager instanceManager;
    private volatile Set<String> remoteAppName = Collections.emptySet();

    public ClusterManager(List<RouterFilter> routerFilterList, LoadBalancer loadBalancer, InstanceManager instanceManager) {
        this.routerFilterList = routerFilterList;
        this.loadBalancer = loadBalancer;
        this.instanceManager = instanceManager;
    }

    public Set<String> getRemoteAppName() {
        return remoteAppName;
    }

    public ClusterManager(List<RouterFilter> routerFilterList) {
        this.routerFilterList = routerFilterList;
    }

    public ClusterManager(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    /**
     * Select one invoker in list, routing first, then load balancing.
     *
     * @param context traffic context.
     * @return selected instance.
     */
    public Instance selectOneInstance(TrafficContext context) {
        return loadBalancer.select(route(context), context);
    }

    /**
     * Select one invoker in list.
     *
     * @param context   traffic context.
     * @param instances instance List.
     * @return selected instance.
     */
    public Instance loadBalance(TrafficContext context, List<Instance> instances) {
        return loadBalancer.select(instances, context);
    }

    public Instance loadBalance(TrafficContext context) {
        return loadBalancer.select(instanceManager.getInstances(), context);
    }

    /**
     * Filter instances form instanceManager by current routing rule and traffic context.
     *
     * @param context traffic context
     * @return instance List
     */
    public List<Instance> route(TrafficContext context) {
        List<Instance> instances = instanceManager.getInstances();
        for (RouterFilter routerFilter : routerFilterList) {
            instances = routerFilter.filter(instances, context);
        }
        return instances;
    }

    /**
     * Register the application in the instance List.
     *
     * @param instances instance List
     */
    private void registerAppRule(List<Instance> instances) {
        Set<String> currentApplication = new HashSet<>();
        if (instances != null && instances.size() > 0) {
            for (Instance instance : instances) {
                String applicationName = instance.getAppName();
                if (applicationName != null && applicationName.length() > 0) {
                    currentApplication.add(applicationName);
                }
            }
        }

        if (!remoteAppName.equals(currentApplication)) {
            synchronized (this) {
                Set<String> current = new HashSet<>(currentApplication);
                Set<String> previous = new HashSet<>(remoteAppName);
                previous.removeAll(currentApplication);
                current.removeAll(remoteAppName);
                for (String app : current) {
                    TrafficRouterRuleManager.register(app);
                }
                for (String app : previous) {
                    TrafficRouterRuleManager.unregister(app);
                }
                remoteAppName = currentApplication;
            }
        }
    }

    @Override
    public void notify(List<Instance> instances) {
        registerAppRule(instances);
        instanceManager.storeInstances(instances);
    }
}
