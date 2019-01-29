/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.codec;

/**
 * A universal interface for publishing entities to a target stream.
 *
 * @param <E> entity type
 * @param <T> target stream type
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface EntityWriter<E, T> {

    /**
     * Write the provided entity to target stream.
     *
     * @param entity entity to publish
     * @param target the target stream
     */
    void writeTo(E entity, T target);
}
