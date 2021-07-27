package com.alibaba.csp.sentinel.slots.block;

import java.util.List;

/**
 * @author : jiez
 * @date : 2021/7/17 11:03
 */
public interface RuleSelector<T> {

    /**
     * Return Supported Rule Type list
     *
     * @return list of supported Rule Type
     *
     * @see RuleConstant prefix: RULE_SELECTOR_TYPE_
     */
    List<String> getSupportedRuleTypes();

    /**
     * Return this rule selector priority
     *
     * @return int of priority
     */
    int getPriority();

    /**
     * Return to the list of selected rules
     *
     * @param resource resource name
     * @return list of selected Rule
     */
    List<T> select(String resource);
}
