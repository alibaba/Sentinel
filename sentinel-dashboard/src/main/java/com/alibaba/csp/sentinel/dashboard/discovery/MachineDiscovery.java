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

import java.util.List;
import java.util.Set;

public interface MachineDiscovery {

    String UNKNOWN_APP_NAME = "CLUSTER_NOT_STARTED";

    List<String> getAppNames();

    Set<AppInfo> getBriefApps();

    AppInfo getDetailApp(String app);

    /**
     * Remove the given app from the application registry.
     *
     * @param app application name
     * @since 1.5.0
     */
    void removeApp(String app);

    long addMachine(MachineInfo machineInfo);

    /**
     * Remove the given machine instance from the application registry.
     *
     * @param app the application name of the machine
     * @param ip machine IP
     * @param port machine port
     * @return true if removed, otherwise false
     * @since 1.5.0
     */
    boolean removeMachine(String app, String ip, int port);
}