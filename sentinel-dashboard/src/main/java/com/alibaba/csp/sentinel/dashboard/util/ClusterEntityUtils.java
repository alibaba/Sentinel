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
package com.alibaba.csp.sentinel.dashboard.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterGroupEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.ConnectionGroupVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.AppClusterClientStateWrapVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.AppClusterServerStateWrapVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterClientStateVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterServerStateVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterUniversalStatePairVO;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public final class ClusterEntityUtils {

    public static List<AppClusterServerStateWrapVO> wrapToAppClusterServerState(
        List<ClusterUniversalStatePairVO> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, AppClusterServerStateWrapVO> map = new HashMap<>();
        for (ClusterUniversalStatePairVO stateVO : list) {
            int mode = stateVO.getState().getStateInfo().getMode();

            if (mode == ClusterStateManager.CLUSTER_SERVER) {
                String ip = stateVO.getIp();
                String serverId = ip + '@' + stateVO.getCommandPort();
                ClusterServerStateVO serverStateVO = stateVO.getState().getServer();
                map.computeIfAbsent(serverId, v -> new AppClusterServerStateWrapVO()
                    .setId(serverId)
                    .setIp(ip)
                    .setPort(serverStateVO.getPort())
                    .setState(serverStateVO)
                    .setConnectedCount(serverStateVO.getConnection().stream()
                        .mapToInt(ConnectionGroupVO::getConnectedCount)
                        .sum()
                    )
                );
            }
        }
        return new ArrayList<>(map.values());
    }

    public static List<AppClusterClientStateWrapVO> wrapToAppClusterClientState(
        List<ClusterUniversalStatePairVO> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, AppClusterClientStateWrapVO> map = new HashMap<>();
        for (ClusterUniversalStatePairVO stateVO : list) {
            int mode = stateVO.getState().getStateInfo().getMode();

            if (mode == ClusterStateManager.CLUSTER_CLIENT) {
                String ip = stateVO.getIp();
                String clientId = ip + '@' + stateVO.getCommandPort();
                ClusterClientStateVO clientStateVO = stateVO.getState().getClient();
                map.computeIfAbsent(clientId, v -> new AppClusterClientStateWrapVO()
                    .setId(clientId)
                    .setIp(ip)
                    .setState(clientStateVO)
                    .setCommandPort(stateVO.getCommandPort())
                );
            }
        }
        return new ArrayList<>(map.values());
    }

    public static List<ClusterGroupEntity> wrapToClusterGroup(List<ClusterUniversalStatePairVO> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, ClusterGroupEntity> map = new HashMap<>();
        for (ClusterUniversalStatePairVO stateVO : list) {
            int mode = stateVO.getState().getStateInfo().getMode();
            String ip = stateVO.getIp();
            if (mode == ClusterStateManager.CLUSTER_SERVER) {
                String serverAddress = getIp(ip);
                int port = stateVO.getState().getServer().getPort();
                map.computeIfAbsent(serverAddress, v -> new ClusterGroupEntity()
                    .setBelongToApp(true).setMachineId(ip + '@' + stateVO.getCommandPort())
                    .setIp(ip).setPort(port)
                );
            }
        }
        for (ClusterUniversalStatePairVO stateVO : list) {
            int mode = stateVO.getState().getStateInfo().getMode();
            String ip = stateVO.getIp();
            if (mode == ClusterStateManager.CLUSTER_CLIENT) {
                String targetServer = stateVO.getState().getClient().getClientConfig().getServerHost();
                Integer targetPort = stateVO.getState().getClient().getClientConfig().getServerPort();
                if (StringUtil.isBlank(targetServer) || targetPort == null || targetPort <= 0) {
                    continue;
                }

                ClusterGroupEntity group = map.computeIfAbsent(targetServer,
                    v -> new ClusterGroupEntity()
                        .setBelongToApp(true).setMachineId(targetServer)
                        .setIp(targetServer).setPort(targetPort)
                );
                group.getClientSet().add(ip + '@' + stateVO.getCommandPort());
            }
        }
        return new ArrayList<>(map.values());
    }

    private static String getIp(String str) {
        if (str.contains(":")) {
            return str.split(":")[0];
        }
        return str;
    }

    private ClusterEntityUtils() {}
}
