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
package com.alibaba.csp.sentinel.demo.file.rule.parser;

import java.util.List;

import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * A {@link ConfigParser} parses Json String to {@code List<SystemRule>}.
 *
 * @author Carpenter Lee
 */
public class JsonSystemRuleListParser implements ConfigParser<String, List<SystemRule>> {
    @Override
    public List<SystemRule> parse(String source) {
        return JSON.parseObject(source, new TypeReference<List<SystemRule>>() {});
    }
}
