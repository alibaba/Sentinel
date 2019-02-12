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
package com.alibaba.csp.sentinel.dashboard.service;

import java.util.List;
import java.util.Set;

import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterAppAssignResultVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public interface ClusterAssignService {

    /**
     * Unbind a specific cluster server and its clients.
     *
     * @param app app name
     * @param machineId valid machine ID ({@code host@commandPort})
     * @return assign result
     */
    ClusterAppAssignResultVO unbindClusterServer(String app, String machineId);

    /**
     * Unbind a set of cluster servers and its clients.
     *
     * @param app app name
     * @param machineIdSet set of valid machine ID ({@code host@commandPort})
     * @return assign result
     */
    ClusterAppAssignResultVO unbindClusterServers(String app, Set<String> machineIdSet);

    /**
     * Apply cluster server and client assignment for provided app.
     *
     * @param app app name
     * @param clusterMap cluster assign map (server -> clients)
     * @param remainingSet unassigned set of machine ID
     * @return assign result
     */
    ClusterAppAssignResultVO applyAssignToApp(String app, List<ClusterAppAssignMap> clusterMap,
                                              Set<String> remainingSet);
}
