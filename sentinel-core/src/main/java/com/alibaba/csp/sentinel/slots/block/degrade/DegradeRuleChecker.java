package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;

public class DegradeRuleChecker {

    /**
     * @param rule         DegradeRule
     * @param context      context
     * @param entranceNode entranceNode
     * @return select Node by limitApp. chain for entranceNode.other for clusterNode
     */
    public static Node selectNodeByLimitApp(/*@NonNull*/ DegradeRule rule, Context context, DefaultNode entranceNode) {
        String limitApp = rule.getLimitApp();
        if (StringUtil.isNotBlank(limitApp) && RuleConstant.LIMIT_APP_ORIGIN.equals(limitApp)) {
            if (null != context.getOriginNode()) {
                return context.getOriginNode();
            }
        }
        return ClusterBuilderSlot.getClusterNode(rule.getResource());
    }

}
