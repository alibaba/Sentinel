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

import java.util.Objects;

public class LoggingTrafficShapingControllerFactory implements TrafficShapingControllerFactory {

    private final TrafficShapingControllerFactory delegate;

    public LoggingTrafficShapingControllerFactory(TrafficShapingControllerFactory delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must be not null.");
    }

    @Override
    public TrafficShapingController create(FlowRule rule) {
        RecordLog.debug("Creating traffic shaping controller '" + delegate.getClass().getName() + "' for rule: " + rule);
        return delegate.create(rule);
    }

    @Override
    public int getControlBehavior() {
        return delegate.getControlBehavior();
    }
}
