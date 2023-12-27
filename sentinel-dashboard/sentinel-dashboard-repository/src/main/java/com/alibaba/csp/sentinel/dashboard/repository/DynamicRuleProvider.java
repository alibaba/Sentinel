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
package com.alibaba.csp.sentinel.dashboard.repository;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;

import java.util.List;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface DynamicRuleProvider<T extends RuleEntity> {

    List<T> getRules(String app) throws Exception;

    List<T> getRules(String app, String ip, Integer port) throws Exception;
}
