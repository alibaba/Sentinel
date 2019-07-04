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
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Component("flowRuleDefaultPublisher")
public class FlowRuleApiPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

    private final Logger logger = LoggerFactory.getLogger(FlowRuleApiPublisher.class);


    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private AppManagement appManagement;

    @Override
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        if (StringUtil.isBlank(app)) {
            return;
        }
        if (rules == null) {
            return;
        }
        AppInfo appInfo = appManagement.getDetailApp(app);
        if (appInfo == null) {
            logger.error("could not find app:{} info", app);
            return;
        }
        Set<MachineInfo> machineInfos = appInfo.getMachines();
        if (!CollectionUtils.isEmpty(machineInfos)) {
            machineInfos.stream().filter(MachineInfo::isHealthy).parallel()
                    .forEach(machine -> sentinelApiClient.setFlowRuleOfMachine(app, machine.getIp(), machine.getPort(), rules));
        } else {
            logger.warn("app: {} no machines found to publish", app);
        }

    }
}
