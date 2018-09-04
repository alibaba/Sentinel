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
package com.alibaba.csp.sentinel.datasource;

/**
 * Convert an object from source type {@code S} to target type {@code T}.
 *
 * @author leyou
 * @author Eric Zhao
 */
public interface Converter<S, T> {

    /**
     * Convert {@code source} to the target type.
     *
     * @param source the source object
     * @return the target object
     */
    T convert(S source);
}
