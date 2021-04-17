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
package com.alibaba.csp.sentinel.dashboard.converter;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wxq
 */
public class RuleConverter {

    public static List<? extends Rule> convert2RuleList(List<? extends RuleEntity> entities) {
        List<Rule> rules = new ArrayList<>();
        for (RuleEntity ruleEntity : entities) {
            Rule rule = ruleEntity.toRule();
            rules.add(rule);
        }
        return Collections.unmodifiableList(rules);
    }

}
