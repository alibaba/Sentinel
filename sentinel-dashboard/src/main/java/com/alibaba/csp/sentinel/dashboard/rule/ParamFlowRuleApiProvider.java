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

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author lianglin
 * @since 1.7.0
 */

@Component("paramFlowRuleDefaultProvider")
public class ParamFlowRuleApiProvider extends AbstractDynamicRuleProvider<List<ParamFlowRuleEntity>> {

    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private AppManagement appManagement;

    @Override
    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return new ArrayList<>();
        }
        Set<MachineInfo> machineInfos = appManagement.getDetailApp(appName).getMachines();
        List<MachineInfo> list = sortMachineInfos(machineInfos);
        if (list.isEmpty()) {
            return new ArrayList<>();
        } else {
            MachineInfo machine = list.get(0);
            CompletableFuture<List<ParamFlowRuleEntity>> future = sentinelApiClient.fetchParamFlowRulesOfMachine(machine.getApp(), machine.getIp(), machine.getPort());
            return future.get();
        }
    }
}
