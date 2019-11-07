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
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public interface SphResourceTypeSupport {

    /**
     * Create a protected resource with provided classification.
     *
     * @param name the unique name of the protected resource
     * @param resourceType the classification of the resource
     * @param entryType the traffic entry type (IN/OUT) of the resource
     * @param count tokens required
     * @param args  extra parameters
     * @return new entry of the resource
     * @throws BlockException if the block criteria is met
     */
    Entry entryWithType(String name, int resourceType, EntryType entryType, int count, Object[] args)
        throws BlockException;

    /**
     * Create a protected resource with provided classification.
     *
     * @param name the unique name of the protected resource
     * @param resourceType the classification of the resource
     * @param entryType the traffic entry type (IN/OUT) of the resource
     * @param count tokens required
     * @param prioritized whether the entry is prioritized
     * @param args  extra parameters
     * @return new entry of the resource
     * @throws BlockException if the block criteria is met
     */
    Entry entryWithType(String name, int resourceType, EntryType entryType, int count, boolean prioritized,
                        Object[] args) throws BlockException;

    /**
     * Create an asynchronous resource with provided classification.
     *
     * @param name the unique name of the protected resource
     * @param resourceType the classification of the resource
     * @param entryType the traffic entry type (IN/OUT) of the resource
     * @param count tokens required
     * @param prioritized whether the entry is prioritized
     * @param args  extra parameters
     * @return new entry of the resource
     * @throws BlockException if the block criteria is met
     */
    AsyncEntry asyncEntryWithType(String name, int resourceType, EntryType entryType, int count, boolean prioritized,
                                  Object[] args) throws BlockException;
}
