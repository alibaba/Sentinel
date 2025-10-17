package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingControllerFactory;

public  class CustomTokenBucketControllerFactory implements TrafficShapingControllerFactory {
  /**
   * Custom control behavior constant - must be >= 256
   */
  public static final int CUSTOM_CONTROL_BEHAVIOR = RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN;
        @Override
        public TrafficShapingController create(FlowRule rule) {
            return new CustomTokenBucketController();
        }

        @Override
        public int getControlBehavior() {
            return CUSTOM_CONTROL_BEHAVIOR;
        }

  public static class CustomTokenBucketController implements TrafficShapingController {


    @Override
    public boolean canPass(Node node, int acquireCount) {
      return canPass(node, acquireCount, false);
    }

    @Override
    public boolean canPass(Node node, int acquireCount, boolean prioritized) {
      if (acquireCount <= 1) return true;
      return false;
    }


  }
}