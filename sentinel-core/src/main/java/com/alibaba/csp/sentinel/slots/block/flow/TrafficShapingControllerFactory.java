/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.flow;

/**
 * a factory interface to create TrafficShapingController instance
 */
public interface TrafficShapingControllerFactory {

    /**
     * create a TrafficShapingController instance
     * @param rule flow rule
     * @return a new TrafficShapingController instance
     */
    TrafficShapingController create(FlowRule rule);

    /**
     * get the factory control behavior
     * @return the control behavior
     */
    int getControlBehavior();

    /**
     * Indicates whether this factory is a built-in Sentinel implementation.
     * Built-in factories are allowed to use control behavior values in the reserved range [0, 255].
     * User-defined factories should return {@code false} (default) and use values >= 256.
     *
     * This method is used internally for validation during factory registration to ensure
     * proper namespace separation and prevent conflicts.
     *
     * @return {@code true} if this is a Sentinel built-in factory, {@code false} for user-defined implementations
     */
    default boolean isBuiltIn() {
        return false;
    }
}
