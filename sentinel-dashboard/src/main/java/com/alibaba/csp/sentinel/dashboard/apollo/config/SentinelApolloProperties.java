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
package com.alibaba.csp.sentinel.dashboard.apollo.config;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.cloud.sentinel.datasource.config.ApolloDataSourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Share those config to other project.
 */
@ConfigurationProperties(prefix = "sentinel.apollo")
@Validated
public class SentinelApolloProperties {

    private static final Logger logger = LoggerFactory.getLogger(SentinelApolloProperties.class);

    private static final String DOT = ".";
    private static final String SENTINEL = "sentinel";
    private static final String DOT_SENTINEL_DOT = DOT + SENTINEL + DOT;

    private static final String DEFAULT_NAMESPACE_NAME = SENTINEL;

    /**
     * default value for {@link #suffix}.
     */
    private static final Map<RuleType, String> DEFAULT_SUFFIX = Collections.unmodifiableMap(
            new TreeMap<RuleType, String>() {{
                for (RuleType ruleType : RuleType.values()) {
                    this.put(ruleType, DOT_SENTINEL_DOT + ruleType.name());
                }
            }}
    );

    /**
     * a private namespace for {@link ApolloDataSourceProperties#getNamespaceName()} used.
     */
    private String namespaceName = DEFAULT_NAMESPACE_NAME;

    /**
     * value will be used in {@link ApolloDataSourceProperties#setFlowRulesKey(String)}'s suffix.
     *
     * @see RuleType#getName() for config with key.
     */
    private final Map<RuleType, String> suffix = new ConcurrentHashMap<>(DEFAULT_SUFFIX);

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        if (! DEFAULT_NAMESPACE_NAME.equals(namespaceName)) {
            logger.info("detect you use custom namespace name, the value will be changed from [{}] to [{}]", DEFAULT_NAMESPACE_NAME, namespaceName);
        }
        this.namespaceName = namespaceName;
    }

    public Map<RuleType, String> getSuffix() {
        return suffix;
    }

    /**
     * custom this method.
     * the behavior will be changed to add or update.
     */
    public void setSuffix(Map<RuleType, String> suffix) {
        for (Map.Entry<RuleType, String> entry : suffix.entrySet()) {
            String defaultValue = DEFAULT_SUFFIX.get(entry.getKey());
            String newValue = entry.getValue();
            if (newValue.equals(defaultValue)) {
                // value not change
            } else {
                logger.info("detect you use custom suffix, the value of key = [{}] will be changed from [{}] to [{}]", entry.getKey(), defaultValue, newValue);
            }
        }
        this.suffix.putAll(suffix);
    }

}
