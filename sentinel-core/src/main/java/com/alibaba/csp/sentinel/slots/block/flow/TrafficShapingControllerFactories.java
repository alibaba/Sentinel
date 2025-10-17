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

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class TrafficShapingControllerFactories {

    private static final Map<Integer, TrafficShapingControllerFactory> FACTORIES;

    static {
        FACTORIES = initFactories();
    }

    /**
     * Using existing factory if the factory with the same control behavior already exists, because the existing factory has higher priority
     */
    private static BinaryOperator<TrafficShapingControllerFactory> usingExisting() {
        return (existing, replacement) -> {
            RecordLog.warn("[TrafficShapingControllerFactories] Duplicate control behavior [{}], " +
                    "using existing factory [{}], ignoring [{}]",
                    existing.getControlBehavior(),
                    existing.getClass().getName(),
                    replacement.getClass().getName());
            return existing;
        };
    }

    private static TrafficShapingControllerFactory logging(TrafficShapingControllerFactory factory) {
        return new LoggingTrafficShapingControllerFactory(factory);
    }

    /**
     * Validates the control behavior namespace of a factory.
     */
    private static void validateControlBehavior(TrafficShapingControllerFactory factory) {
        int controlBehavior = factory.getControlBehavior();
        
        // User-defined factories must not use reserved range [0, 255]
        if (!factory.isBuiltIn() && isReservedControlBehavior(controlBehavior)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid control behavior [%d] for factory [%s]. " +
                    "Control behavior values in range [0, %d] are reserved for Sentinel built-in implementations. " +
                    "User-defined factories must use values >= %d to ensure compatibility with future Sentinel upgrades.",
                    controlBehavior,
                    factory.getClass().getName(),
                    RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN - 1,
                    RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN));
        }
        
        RecordLog.info("[TrafficShapingControllerFactories] Registered factory [{}] for control behavior [{}]",
                factory.getClass().getName(), controlBehavior);
    }

    private static Map<Integer, TrafficShapingControllerFactory> initFactories() {
        List<TrafficShapingControllerFactory> factories = SpiLoader.of(TrafficShapingControllerFactory.class)
                .loadInstanceListSorted();
        // Validate all factories
        for (TrafficShapingControllerFactory factory : factories) {
            validateControlBehavior(factory);
        }
        return factories.stream()
                .collect(Collectors.toMap(
                        TrafficShapingControllerFactory::getControlBehavior,
                        TrafficShapingControllerFactories::logging,
                        usingExisting(),
                        HashMap::new));
    }

    public static TrafficShapingControllerFactory get(int controlBehavior) {
        return FACTORIES.get(controlBehavior);
    }

    private TrafficShapingControllerFactories() {
    }

    public static boolean isReservedControlBehavior(int controlBehavior) {
        return controlBehavior < RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN;
    }
}
