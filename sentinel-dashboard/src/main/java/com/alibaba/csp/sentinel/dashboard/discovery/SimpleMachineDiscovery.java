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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.csp.sentinel.util.AssertUtil;

import org.springframework.stereotype.Component;

/**
 * @author leyou
 */
@Component
public class SimpleMachineDiscovery implements MachineDiscovery {

    private final ConcurrentMap<String, AppInfo> apps = new ConcurrentHashMap<>();

    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        /*
             只有在当前 Map 中 key 对应的值不存在或为 null 时
             才调用 mappingFunction
             并在 mappingFunction 执行结果非 null 时
             将结果跟 key 关联．
             mappingFunction 为空时 将抛出空指针异常
        */
        AppInfo appInfo = apps.computeIfAbsent(machineInfo.getApp(), o -> new AppInfo(machineInfo.getApp(), machineInfo.getAppType()));
        appInfo.addMachine(machineInfo);
        return 1;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        AppInfo appInfo = apps.get(app);
        if (appInfo != null) {
            return appInfo.removeMachine(ip, port);
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        return new ArrayList<>(apps.keySet());
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        return apps.get(app);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return new HashSet<>(apps.values());
    }

    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        apps.remove(app);
    }

}
