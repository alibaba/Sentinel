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
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingControllerFactory;

public class WarmUpControllerFactory implements TrafficShapingControllerFactory {

    static volatile int coldFactor = SentinelConfig.coldFactor();

    @Override
    public TrafficShapingController create(FlowRule rule) {
        return new WarmUpController(rule.getCount(), rule.getWarmUpPeriodSec(), coldFactor);
    }

    @Override
    public int getControlBehavior() {
        return RuleConstant.CONTROL_BEHAVIOR_WARM_UP;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }
}
