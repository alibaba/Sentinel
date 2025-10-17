/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.client.ha;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author icodening
 * @date 2022.03.06
 */
public class DefaultLoadBalanceContext implements LoadBalanceContext {

    private final Map<String, Object> attributes = new LinkedHashMap<>(8);

    private final String clientName;

    public DefaultLoadBalanceContext(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public void setAttribute(String name, Object attribute) {
        if (name == null || attribute == null) {
            return;
        }
        attributes.put(name, attribute);
    }

    @Override
    public Object getAttribute(String name) {
        if (name == null) {
            return null;
        }
        return attributes.get(name);
    }

    @Override
    public <T> T getAttribute(String name, Class<T> returnType) {
        Object attribute = getAttribute(name);
        if (returnType == null || attribute == null) {
            return null;
        }
        if (returnType.isAssignableFrom(attribute.getClass())) {
            return returnType.cast(attribute);
        }
        return null;
    }
}
