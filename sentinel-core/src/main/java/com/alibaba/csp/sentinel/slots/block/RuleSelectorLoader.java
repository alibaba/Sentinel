package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.slots.block.authority.DefaultAuthorityRuleSelector;
import com.alibaba.csp.sentinel.slots.block.degrade.DefaultDegradeRuleSelector;
import com.alibaba.csp.sentinel.slots.block.flow.DefaultFlowRuleSelector;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.*;

/**
 * @author : jiez
 * @date : 2021/7/17 14:39
 */
public class RuleSelectorLoader {

    private volatile static List<RuleSelector> selectors = null;

    public static RuleSelector getSelector(String useType) {
        RuleSelector highestPrioritySelector = getHighestPrioritySelector(useType);
        if (Objects.isNull(highestPrioritySelector)) {
            return getDefaultSelector(useType);
        }
        return highestPrioritySelector;
    }

    public static RuleSelector getHighestPrioritySelector(String useType) {
        List<RuleSelector> selectors = getSelector(useType, true);
        if (Objects.isNull(selectors) || selectors.size() <= 0) {
            return null;
        }
        selectors.sort(Comparator.comparingInt(RuleSelector::getPriority));
        return selectors.get(0);
    }

    public static List<RuleSelector> getSelector(String useType, boolean reloadWhenNoExist) {
        if ((Objects.isNull(selectors) || selectors.size() == 0) && reloadWhenNoExist) {
            loadRuleSelector();
        }
        if (selectors.size() == 0) {
            return null;
        }
        List<RuleSelector> matchedSelectorList = new ArrayList<>();
        for (RuleSelector selector : selectors) {
            List supportedRuleTypes = selector.getSupportedRuleTypes();
            if (Objects.isNull(supportedRuleTypes) || supportedRuleTypes.size() <= 0 || !supportedRuleTypes.contains(useType)) {
                continue;
            }
            matchedSelectorList.add(selector);
        }
        return matchedSelectorList;
    }

    private synchronized static void loadRuleSelector() {
        selectors = SpiLoader.of(RuleSelector.class).loadInstanceListSorted();
    }

    private static RuleSelector getDefaultSelector(String useType) {
        switch (useType) {
            case RuleConstant.RULE_SELECTOR_TYPE_FLOW_RULE:
                return new DefaultFlowRuleSelector();
            case RuleConstant.RULE_SELECTOR_TYPE_DEGRADE_RULE:
                return new DefaultDegradeRuleSelector();
            case RuleConstant.RULE_SELECTOR_TYPE_AUTHORITY_RULE:
                return new DefaultAuthorityRuleSelector();
            default:
                return null;
        }
    }
}
