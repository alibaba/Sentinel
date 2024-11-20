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

import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.HashMap;
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
        return (existing, replacement) -> existing;
    }

    private static TrafficShapingControllerFactory logging(TrafficShapingControllerFactory factory) {
        return new LoggingTrafficShapingControllerFactory(factory);
    }

    private static Map<Integer, TrafficShapingControllerFactory> initFactories() {
        return SpiLoader.of(TrafficShapingControllerFactory.class)
                .loadInstanceListSorted()
                .stream()
                .collect(Collectors.toMap(TrafficShapingControllerFactory::getControlBehavior,
                        TrafficShapingControllerFactories::logging,
                        usingExisting(),
                        HashMap::new));
    }

    public static TrafficShapingControllerFactory get(int controlBehavior) {
        return FACTORIES.get(controlBehavior);
    }

    private TrafficShapingControllerFactories() {
    }

}
