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
package com.alibaba.csp.sentinel.slotchain;

import com.alibaba.csp.sentinel.EntryType;

/**
 * A wrapper of resource name and {@link EntryType}.
 *
 * @author qinan.qn
 * @author jialiang.linjl
 */
public abstract class ResourceWrapper {

    protected String name;
    protected EntryType type = EntryType.OUT;

    public abstract String getName();

    public abstract String getShowName();

    /**
     * Get {@link EntryType} of this wrapper.
     *
     * @return {@link EntryType} of this wrapper.
     */
    public abstract EntryType getType();

    /**
     * Only {@link #getName()} is considered.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Only {@link #getName()} is considered.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResourceWrapper) {
            ResourceWrapper rw = (ResourceWrapper)obj;
            return rw.getName().equals(getName());
        }
        return false;
    }
}
