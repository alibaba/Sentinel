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

import java.lang.reflect.Method;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.util.IdUtil;
import com.alibaba.csp.sentinel.util.MethodUtil;

/**
 * Resource wrapper for method invocation.
 *
 * @author qinan.qn
 */
public class MethodResourceWrapper extends ResourceWrapper {

    private final transient Method method;

    public MethodResourceWrapper(Method method, EntryType e) {
        this(method, e, ResourceTypeConstants.COMMON);
    }

    public MethodResourceWrapper(Method method, EntryType e, int resType) {
        super(MethodUtil.resolveMethodName(method), e, resType);
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String getShowName() {
        return name;
    }

    @Override
    public String toString() {
        return "MethodResourceWrapper{" +
            "name='" + name + '\'' +
            ", entryType=" + entryType +
            ", resourceType=" + resourceType +
            '}';
    }
}
