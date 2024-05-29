/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.datasource.xds.property.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lwj
 * @since 2.0.0
 */
public abstract class Repository<T> {
    protected final AtomicReference<T> instance;

    private List<RepositoryUpdateCallback<T>> callbackList = new ArrayList<>();

    public Repository() {
        this(null);
    }

    public Repository(T initInstance) {
        instance = new AtomicReference<>(initInstance);
    }

    public T getInstance() {
        return instance.get();
    }

    public void registryRepositoryUpdateCallback(RepositoryUpdateCallback<T> callback) {
        callbackList.add(callback);
    }

    public void update(T newInstance) {
        instance.set(newInstance);
        for (RepositoryUpdateCallback<T> callback : callbackList) {
            callback.onUpdateCert(newInstance);
        }
        updateEnd(newInstance);
    }

    protected abstract void updateEnd(T newInstance);
}
