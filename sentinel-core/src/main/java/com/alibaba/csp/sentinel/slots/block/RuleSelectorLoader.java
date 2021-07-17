package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.slots.block.flow.DefaultFlowRuleSelector;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRulePropertyListener;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.*;

/**
 * @author : jiez
 * @date : 2021/7/17 14:39
 */
public class RuleSelectorLoader {

    private static List<RuleSelector> selectors;

    public static RuleSelector getSelector(String useType) {
        RuleSelector highestPrioritySelector = getHighestPrioritySelector(useType);
        if (Objects.isNull(highestPrioritySelector)) {
            return new DefaultFlowRuleSelector();
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
        if (selectors.size() == 0 && reloadWhenNoExist) {
            loadRuleSelector();
        }
        if (selectors.size() == 0) {
            return null;
        }
        List<RuleSelector> matchedSelectorList = new ArrayList<>();
        for (RuleSelector selector : selectors) {
            List supportedRuleTypes = selector.getSupportedRuleTypes();
            if (Objects.isNull(supportedRuleTypes) || supportedRuleTypes.size() <= 0 || supportedRuleTypes.contains(useType)) {
                continue;
            }
            matchedSelectorList.add(selector);
        }
        return matchedSelectorList;
    }

    private synchronized static void loadRuleSelector() {
        selectors = new ArrayList<>();
        List<RuleSelector> ruleSelectors = SpiLoader.of(RuleSelector.class).loadInstanceListSorted();
        selectors.addAll(ruleSelectors);
    }

}
