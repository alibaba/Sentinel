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
import com.alibaba.csp.sentinel.ResourceTypeConstants;

/**
 * Common string resource wrapper.
 *
 * @author qinan.qn
 * @author jialiang.linjl
 */
public class StringResourceWrapper extends ResourceWrapper {

    public StringResourceWrapper(String name, EntryType e) {
        super(name, e, ResourceTypeConstants.COMMON);
    }

    public StringResourceWrapper(String name, EntryType e, int resType) {
        super(name, e, resType);
    }

    @Override
    public String getShowName() {
        return name;
    }

    @Override
    public String toString() {
        return "StringResourceWrapper{" +
            "name='" + name + '\'' +
            ", entryType=" + entryType +
            ", resourceType=" + resourceType +
            '}';
    }
}
