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

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 */
public class ContextConfig {

    private final String contextName;
    private final String origin;

    public ContextConfig(String contextName) {
        this(contextName, "");
    }

    public ContextConfig(String contextName, String origin) {
        AssertUtil.assertNotBlank(contextName, "contextName cannot be blank");
        this.contextName = contextName;
        if (StringUtil.isBlank(origin)) {
            origin = "";
        }
        this.origin = origin;
    }

    public String getContextName() {
        return contextName;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "ContextConfig{" +
            "contextName='" + contextName + '\'' +
            ", origin='" + origin + '\'' +
            '}';
    }
}
