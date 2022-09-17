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
package com.alibaba.csp.sentinel.dashboard.repository.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Store {@link FlowRuleEntity} in memory.
 *
 * @author leyou
 */
@Component
@Primary
public class NacosFlowRuleStore extends NacosRuleRepositoryAdapter<FlowRuleEntity> {

    private static AtomicLong ids = new AtomicLong(0);

    public NacosFlowRuleStore(@Qualifier("flowRuleNacosProvider") DynamicRuleProvider<List<FlowRuleEntity>> ruleProvider, @Qualifier("flowRuleNacosPublisher") DynamicRulePublisher<List<FlowRuleEntity>> rulePublisher) {
        super(ruleProvider, rulePublisher);
    }

    @Override
    protected long nextId() {
        return ids.incrementAndGet();
    }

    @Override
    protected FlowRuleEntity preProcess(FlowRuleEntity entity) {
        if (entity != null && entity.isClusterMode()) {
            ClusterFlowConfig config = entity.getClusterConfig();
            if (config == null) {
                config = new ClusterFlowConfig();
                entity.setClusterConfig(config);
            }
            // Set cluster rule id.
            config.setFlowId(entity.getId());
        }
        return entity;
    }
}
