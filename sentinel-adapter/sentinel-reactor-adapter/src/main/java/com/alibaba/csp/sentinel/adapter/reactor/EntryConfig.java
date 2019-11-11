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

import java.util.Arrays;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.5.0
 */
public class EntryConfig {

    private final String resourceName;
    private final EntryType entryType;
    private final int resourceType;

    private final int acquireCount;
    private final Object[] args;
    private final ContextConfig contextConfig;

    public EntryConfig(String resourceName) {
        this(resourceName, EntryType.OUT);
    }

    public EntryConfig(String resourceName, EntryType entryType) {
        this(resourceName, entryType, null);
    }

    public EntryConfig(String resourceName, EntryType entryType, ContextConfig contextConfig) {
        this(resourceName, entryType, 1, new Object[0], contextConfig);
    }

    public EntryConfig(String resourceName, int resourceType, EntryType entryType, ContextConfig contextConfig) {
        this(resourceName, resourceType, entryType, 1, new Object[0], contextConfig);
    }

    public EntryConfig(String resourceName, EntryType entryType, int acquireCount, Object[] args) {
        this(resourceName, entryType, acquireCount, args, null);
    }

    public EntryConfig(String resourceName, EntryType entryType, int acquireCount, Object[] args,
                       ContextConfig contextConfig) {
        this(resourceName, ResourceTypeConstants.COMMON, entryType, acquireCount, args, contextConfig);
    }

    public EntryConfig(String resourceName, int resourceType, EntryType entryType, int acquireCount, Object[] args) {
        this(resourceName, resourceType, entryType, acquireCount, args, null);
    }

    public EntryConfig(String resourceName, int resourceType, EntryType entryType, int acquireCount, Object[] args,
                       ContextConfig contextConfig) {
        AssertUtil.assertNotBlank(resourceName, "resourceName cannot be blank");
        AssertUtil.notNull(entryType, "entryType cannot be null");
        AssertUtil.isTrue(acquireCount > 0, "acquireCount should be positive");
        this.resourceName = resourceName;
        this.entryType = entryType;
        this.resourceType = resourceType;
        this.acquireCount = acquireCount;
        this.args = args;
        // Constructed ContextConfig should be valid here. Null is allowed here.
        this.contextConfig = contextConfig;
    }

    public String getResourceName() {
        return resourceName;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public int getAcquireCount() {
        return acquireCount;
    }

    public Object[] getArgs() {
        return args;
    }

    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    /**
     * @since 1.7.0
     */
    public int getResourceType() {
        return resourceType;
    }

    @Override
    public String toString() {
        return "EntryConfig{" +
            "resourceName='" + resourceName + '\'' +
            ", entryType=" + entryType +
            ", resourceType=" + resourceType +
            ", acquireCount=" + acquireCount +
            ", args=" + Arrays.toString(args) +
            ", contextConfig=" + contextConfig +
            '}';
    }
}
