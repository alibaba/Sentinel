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
package com.alibaba.csp.sentinel.demo.file.rule;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.net.URLDecoder;
import java.util.List;

/**
 * This demo is used to check if fastjson supports parsing both int value and string value to enum
 * @author Weihua
 * @since 1.7.0
 */
public class JsonConverterDemo {

    public static void main(String[] args) throws Exception {
        new JsonConverterDemo().parse();
    }

    private void parse() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String authorityPath = URLDecoder.decode(classLoader.getResource("AuthorityRule.json").getFile(), "UTF-8");

        // Data source for SystemRule
        FileRefreshableDataSource<List<AuthorityRule>> authorityRuleDataSource
            = new FileRefreshableDataSource<>(
            authorityPath, authorityRuleListParser);
        AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());
        System.out.println(AuthorityRuleManager.getRules());
    }

    private Converter<String, List<AuthorityRule>> authorityRuleListParser = source -> JSON.parseObject(source,
        new TypeReference<List<AuthorityRule>>() {});
}
