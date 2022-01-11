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
package com.alibaba.csp.sentinel.datasource.converter;

import java.util.Optional;

/**
 * Convert an object from DataSource type {@code D} to Sentinel type {@code S}.
 *
 * @param <D> data type for DataSource
 * @param <S> data type for Sentinel
 *
 * @author Jiajiangnan
 */
public interface SentinelConverter<D, S> {

    /**
     * Convert {@code source} to the target type for Sentinel
     * Default convert is cast by force
     *
     * @param source DataSource Type
     *
     * @return {@code S} Sentinel Type
     */
    default S toSentinel(D source) {
        return Optional.ofNullable(source).isPresent() ? (S)source : null;
    }

    /**
     * Convert {@code source} from the target type for Sentinel
     * Default convert is cast by force
     *
     * @param source Sentinel Type
     *
     * @return {@code D} DataSource Type
     */
    default D fromSentinel(S source)  {
        return Optional.ofNullable(source).isPresent() ? (D) source : null;
    }

}
