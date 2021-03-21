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
package com.alibaba.csp.sentinel.dashboard.apollo.util;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;

/**
 *
 */
public interface DataSourceConverterUtils {

    Converter<List<? extends Rule>, String> SERIALIZER = rule -> JSON.toJSONString(rule, true);

    BiFunction<String, RuleType, List<? extends Rule>> DESERIALIZER = (json, ruleType) -> {
        Class<? extends Rule> ruleClazz = ruleType.getClazz();
        return JSON.parseArray(json, ruleClazz);
    };

    static String serializeToString(List<? extends Rule> rules) {
        return SERIALIZER.convert(rules);
    }

    static List<? extends Rule> deserialize(String content, RuleType ruleType) {
        return DESERIALIZER.apply(content, ruleType);
    }

    static List<? extends Rule> deserialize(byte[] bytes, RuleType ruleType) {
        String content = new String(bytes, StandardCharsets.UTF_8);
        return DESERIALIZER.apply(content, ruleType);
    }

}
