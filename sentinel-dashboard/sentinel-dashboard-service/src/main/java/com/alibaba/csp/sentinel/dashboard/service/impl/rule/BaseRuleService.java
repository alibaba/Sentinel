/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.repository.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.service.impl.BaseService;
import com.alibaba.csp.sentinel.dashboard.service.impl.common.id.IdGenerator;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author cdfive
 */
public class BaseRuleService<T extends RuleEntity> extends BaseService {

    @Autowired
    protected IdGenerator idGenerator;

    @Autowired
    private DynamicRuleProvider<T> ruleProvider;

    @Autowired
    protected DynamicRulePublisher<T> rulePublisher;

    protected List<T> queryRuleList(MachineReqVo reqVo) throws Exception {
        List<T> rules = fetchRules(reqVo);

        rules.forEach(rule -> {
            if (isOperateApp(reqVo)) {
                rule.setIp(null);
                rule.setPort(null);
            } else {
                rule.setIp(reqVo.getIp());
                rule.setPort(reqVo.getPort());
            }
        });

        return rules;
    }

    protected T addRule(MachineReqVo reqVo, T rule) throws Exception {
        List<T> rules = fetchRules(reqVo);

        rule.setId(idGenerator.nextLongId());
        rules.add(rule);

        publishRules(reqVo, rules);
        return rule;
    }

    protected T updateRule(MachineReqVo reqVo, Long id, UpdateRuleCallback<T> updateRuleCallback) throws Exception {
        List<T> rules = fetchRules(reqVo);

        Optional<T> optRule = rules.stream().filter(o -> id.equals(o.getId())).findFirst();
        if (!optRule.isPresent()) {
            fail("data not exist, id=" + id);
        }

        T rule = optRule.get();
        updateRuleCallback.doUpdateRule(rule);

        publishRules(reqVo, rules);
        return rule;
    }

    protected void deleteRule(MachineReqVo reqVo, Long id) throws Exception {
        List<T> rules = fetchRules(reqVo);

        Optional<T> optRule = rules.stream().filter(o -> id.equals(o.getId())).findFirst();
        if (!optRule.isPresent()) {
            fail("data not exist, id=" + id);
        }

        T rule = optRule.get();
        rules.remove(rule);

        publishRules(reqVo, rules);
    }

    protected List<T> fetchRules(MachineReqVo reqVo) throws Exception {
        List<T> rules;
        if (isOperateApp(reqVo)) {
            rules = ruleProvider.getRules(reqVo.getApp());
        } else {
            rules = ruleProvider.getRules(reqVo.getApp(), reqVo.getIp(), reqVo.getPort());
        }
        return rules;
    }

    protected void publishRules(MachineReqVo reqVo, List<T> rules) throws Exception {
        if (isOperateApp(reqVo)) {
            rulePublisher.publish(reqVo.getApp(), rules);
        } else {
            rulePublisher.publish(reqVo.getApp(), reqVo.getIp(), reqVo.getPort(), rules);
        }
    }

    protected boolean isOperateApp(MachineReqVo reqVo) {
        String ip = reqVo.getIp();
        Integer port = reqVo.getPort();
        return StringUtil.isEmpty(ip) || port == null;
    }

    protected interface UpdateRuleCallback<T> {
        void doUpdateRule(T toUpdateRule);
    }
}
