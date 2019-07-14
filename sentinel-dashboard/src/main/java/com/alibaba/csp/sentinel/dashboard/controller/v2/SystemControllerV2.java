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
package com.alibaba.csp.sentinel.dashboard.controller.v2;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author lixiangqian
 */
@RestController
@RequestMapping(value = "/v2/system")
public class SystemControllerV2 extends BaseControllerV2<SystemRuleEntity> {

    private final Logger logger = LoggerFactory.getLogger(SystemControllerV2.class);

    @Autowired
    private InMemoryRuleRepositoryAdapter<SystemRuleEntity> repository;

    @Autowired
    @Qualifier("systemRuleCustomProvider")
    private DynamicRuleProvider<List<SystemRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("systemRuleCustomPublisher")
    private DynamicRulePublisher<List<SystemRuleEntity>> rulePublisher;

    @Override
    protected Logger getLogger() {
        return this.logger;
    }

    @Override
    protected InMemoryRuleRepositoryAdapter<SystemRuleEntity> getRepository() {
        return this.repository;
    }

    @Override
    protected DynamicRuleProvider<List<SystemRuleEntity>> getRuleProvider() {
        return this.ruleProvider;
    }

    @Override
    protected DynamicRulePublisher<List<SystemRuleEntity>> getRulePublisher() {
        return this.rulePublisher;
    }

    @Override
    protected void preSave(SystemRuleEntity entity) {
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
    }

    @Override
    protected void preUpdate(SystemRuleEntity entity, SystemRuleEntity oldEntity) {
        Date date = new Date();
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(date);
    }

    @Override
    protected <R> Result<R> checkEntityInternal(SystemRuleEntity entity) {
        if (entity.getAvgLoad() == null && entity.getAvgRt() == null && entity.getMaxThread() == null && entity.getQps() == null) {
            return Result.ofFail(-1, "avgLoad、avgRt、maxThread or qps must be set at least one");
        }

        return null;
    }
}
