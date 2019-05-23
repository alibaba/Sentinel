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
package com.alibaba.csp.sentinel.cluster.flow.rule;

import java.util.List;

import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;

/**
 * A property wrapper for list of rules of a given namespace.
 * This is useful for auto-management of the property and listener.
 *
 * @param <T> type of the rule
 * @author Eric Zhao
 * @since 1.4.0
 */
class NamespaceFlowProperty<T> {

    private final String namespace;
    private final SentinelProperty<List<T>> property;
    private final PropertyListener<List<T>> listener;

    public NamespaceFlowProperty(String namespace,
                                 SentinelProperty<List<T>> property,
                                 PropertyListener<List<T>> listener) {
        this.namespace = namespace;
        this.property = property;
        this.listener = listener;
    }

    public SentinelProperty<List<T>> getProperty() {
        return property;
    }

    public String getNamespace() {
        return namespace;
    }

    public PropertyListener<List<T>> getListener() {
        return listener;
    }
}
