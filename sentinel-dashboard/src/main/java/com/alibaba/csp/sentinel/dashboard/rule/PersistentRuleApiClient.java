package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.RuleConfigTypeEnum;

import java.util.List;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/31
 * @Version 1.0
 */
public abstract interface PersistentRuleApiClient<T> {

    /**
     * Fetch rules from remote rule configuration center for given application name and ruleType.
     *
     * @param app app name
     * @param configType
     * @return
     * @throws Exception
     */
    List<T> fetch(String app, RuleConfigTypeEnum configType) throws Exception;

    /**
     * Publish rules to remote rule configuration center for given application name and ruleType.
     *
     * @param app app name
     * @param configType
     * @param rules list of rules to push
     * @throws Exception if some error occurs
     */
    void publish(String app, RuleConfigTypeEnum configType, List<T> rules) throws Exception;

    /**
     * Publish rules to remote rule configuration center for given application name and ruleType.
     *
     * @param app app name
     * @param configType
     * @param rules list of rules to push
     * @throws Exception if some error occurs
     * @return
     */
    boolean publishReturnBoolean(String app, RuleConfigTypeEnum configType, List<T> rules);

}
