package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingControllerFactory;
import com.alibaba.csp.sentinel.slots.block.flow.controller.CustomTokenBucketControllerFactory.CustomTokenBucketController;

public  class InvalidReservedBehaviorFactory implements TrafficShapingControllerFactory {

        @Override
        public TrafficShapingController create(FlowRule rule) {
            return new CustomTokenBucketController();
        }

        @Override
        public int getControlBehavior() {
            return 5; // This is in the reserved range [0, 255]
        }

        @Override
        public boolean isBuiltIn() {
            return false; // Not a built-in factory
        }
    }