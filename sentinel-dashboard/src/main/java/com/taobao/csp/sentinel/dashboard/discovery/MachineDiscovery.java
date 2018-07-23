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
package com.taobao.csp.sentinel.dashboard.discovery;

import java.util.List;
import java.util.Set;

public interface MachineDiscovery {

    long MAX_CLIENT_LIVE_TIME_MS = 1000 * 60 * 5;
    String UNKNOWN_APP_NAME = "UNKNOWN";

    List<String> getAppNames();

    Set<AppInfo> getBriefApps();

    AppInfo getDetailApp(String app);

    long addMachine(MachineInfo machineInfo);
}