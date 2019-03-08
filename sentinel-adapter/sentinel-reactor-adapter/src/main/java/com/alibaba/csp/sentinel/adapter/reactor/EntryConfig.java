/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.reactor;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.5.0
 */
public class EntryConfig {

    private final String resourceName;
    private final EntryType entryType;
    private final ContextConfig contextConfig;

    public EntryConfig(String resourceName) {
        this(resourceName, EntryType.OUT);
    }

    public EntryConfig(String resourceName, EntryType entryType) {
        this(resourceName, entryType, null);
    }

    public EntryConfig(String resourceName, EntryType entryType, ContextConfig contextConfig) {
        checkParams(resourceName, entryType);
        this.resourceName = resourceName;
        this.entryType = entryType;
        // Constructed ContextConfig should be valid here. Null is allowed here.
        this.contextConfig = contextConfig;
    }

    public String getResourceName() {
        return resourceName;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    public static void assertValid(EntryConfig config) {
        AssertUtil.notNull(config, "entry config cannot be null");
        checkParams(config.resourceName, config.entryType);
    }

    private static void checkParams(String resourceName, EntryType entryType) {
        AssertUtil.assertNotBlank(resourceName, "resourceName cannot be blank");
        AssertUtil.notNull(entryType, "entryType cannot be null");
    }

    @Override
    public String toString() {
        return "EntryConfig{" +
            "resourceName='" + resourceName + '\'' +
            ", entryType=" + entryType +
            ", contextConfig=" + contextConfig +
            '}';
    }
}
