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
package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;

public class AppInfo {
    private static final Comparator<MachineInfo> COMPARATOR_BY_MACHINE_HEARTBEAT_DESC = new Comparator<MachineInfo>() {

        @Override
        public int compare(MachineInfo o1, MachineInfo o2) {
            if (o1.getLastHeatbeat() < o2.getLastHeatbeat()) {
                return -1;
            }
            if (o1.getLastHeatbeat() > o2.getLastHeatbeat()) {
                return 1;
            }
            return 0;
        }
    };

    private String app = "";

    private Set<MachineInfo> machines = ConcurrentHashMap.newKeySet();

    public AppInfo() {
    }

    public AppInfo(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    /**
     * Get the current machines.
     *
     * @return a new copy of the current machines.
     */
    public Set<MachineInfo> getMachines() {
        return new HashSet<>(machines);
    }

    @Override
    public String toString() {
        return "AppInfo{" + "app='" + app + ", machines=" + machines + '}';
    }

    public boolean addMachine(MachineInfo machineInfo) {
        machines.remove(machineInfo);
        return machines.add(machineInfo);
    }
    
    public synchronized boolean removeMachine(String ip, int port) {
        Iterator<MachineInfo> it = machines.iterator();
        while (it.hasNext()) {
            MachineInfo machine = it.next();
            if (machine.getIp().equals(ip) && machine.getPort() == port) {
                it.remove();
                return true;
            }
        }
        return false;
    }
    
    public Optional<MachineInfo> getMachine(String ip, int port) {
        return machines.stream()
            .filter(e -> e.getIp().equals(ip) && e.getPort().equals(port))
            .findFirst();
    }
    
    private boolean heartbeatJudge(int threshold) {
        if (machines.size() == 0) {
            return false;
        }
        if (threshold > 0) {
            long healthyCount = machines.stream()
                    .filter(m -> m.isHealthy())
                    .count();
            if (healthyCount == 0) {
                // no machine
                long recentHeartBeat = machines.stream()
                        .max(COMPARATOR_BY_MACHINE_HEARTBEAT_DESC).get().getLastHeatbeat();
                return System.currentTimeMillis() - recentHeartBeat < threshold;
            }
        }
        return true;
    }
    
    /**
     * having no healthy machine and should not be displayed
     * 
     * @return
     */
    public boolean isShown() {
        return heartbeatJudge(DashboardConfig.getHideAppNoMachineMillis());
    }
    
    /**
     * having no healthy machine and should be removed
     * 
     * @return
     */
    public boolean isDead() {
        return !heartbeatJudge(DashboardConfig.getRemoveAppNoMachineMillis());
    }
}
