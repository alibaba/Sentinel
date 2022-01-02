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

/**
 * Convert an object from source type for DataSource {@code S} to target type for Sentinel {@code T}.
 *
 * @param <S> source data type for DataSource
 * @param <T> target data type for Sentinel
 *
 * @author Jiajiangnan
 */
public interface SentinelConverter<S, T> {

    /**
     * Convert {@code source} to the target type for Sentinel
     *
     * @param source the source object for DataSource
     * @return the target object for Sentinel
     */
    default T toSentinel(S source) {
        throw new UnsupportedOperationException("method toSentinel musted be Overrided!");
    }

    /**
     * Convert {@code source} from the target type for Sentinel
     *
     * @param source the source for Sentinel
     * @return the target object for DataSource
     */
    default S fromSentinel(T source) {
        throw new UnsupportedOperationException("method fromSentinel musted be Overrided!");
    }

}
