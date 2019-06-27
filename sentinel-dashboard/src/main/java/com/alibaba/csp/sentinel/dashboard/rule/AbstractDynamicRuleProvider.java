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
package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lianglin
 * @since 1.7.0
 */
public abstract class AbstractDynamicRuleProvider<T> implements DynamicRuleProvider {

    protected List<MachineInfo> sortMachineInfos(Collection<MachineInfo> machineInfos) {
        List<MachineInfo> machineInfoList = new ArrayList<>();
        if (machineInfos != null && machineInfos.size() > 0) {
            machineInfoList = machineInfos.stream()
                    .filter(MachineInfo::isHealthy)
                    .sorted((e1, e2) -> Long.compare(e2.getLastHeartbeat(), e1.getLastHeartbeat())).collect(Collectors.toList());
        }
        return machineInfoList;
    }

    /**
     * Get Rules of app
     *
     * @param appName
     * @return
     * @throws Exception
     */
    @Override
    public abstract T getRules(String appName) throws Exception;

}
