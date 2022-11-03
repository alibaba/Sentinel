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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author FengJianxin
 * @since 1.8.4
 */
@Component
public class DynamicRuleStoreFactory {

    private final Map<RuleType, DynamicRuleStore<? extends RuleEntity>> storeMap;

    public DynamicRuleStoreFactory(final List<DynamicRuleStore<? extends RuleEntity>> storeList) {
        Objects.requireNonNull(storeList, "store list must not be null");
        storeMap = new HashMap<>(storeList.size());
        storeList.forEach(item -> storeMap.putIfAbsent(item.getRuleType(), item));
    }

    @SuppressWarnings({"unchecked"})
    public <T extends RuleEntity> DynamicRuleStore<T> getDynamicRuleStoreByType(final RuleType ruleType) {
        DynamicRuleStore<T> store = (DynamicRuleStore<T>) storeMap.get(ruleType);
        if (store == null) {
            throw new RuntimeException("can not find DynamicRuleStore by type: " + ruleType.getName());
        }
        return store;
    }

}
