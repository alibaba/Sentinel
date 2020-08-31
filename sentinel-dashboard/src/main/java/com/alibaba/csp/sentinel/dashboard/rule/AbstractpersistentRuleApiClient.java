package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.RuleConfigTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/31
 * @Version 1.0
 */
public abstract class AbstractpersistentRuleApiClient<T> implements PersistentRuleApiClient<T> {

    private static Logger logger = LoggerFactory.getLogger(AbstractpersistentRuleApiClient.class);

    @Override
    public boolean publishReturnBoolean(String app, RuleConfigTypeEnum configType, List<T> rules) {
        try {
            this.publish(app, configType, rules);
        } catch (Exception ex) {

            logger.warn("setRules API failed: {}", configType.getValue(), ex);
            return false;
        }
        return true;
    }

}
