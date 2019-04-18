package com.alibaba.csp.sentinel.dashboard.controller.config.check;

import com.alibaba.csp.sentinel.dashboard.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * do some check if use Nacos as DataSource
 *
 * @author longqiang
 */
@Component(Constants.CONFIG_CHANGE_CHECKER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "nacos")
public class NacosChecker implements Checker {

    @Override
    public boolean checkOperator(String operator, String app, String ip, Integer port) {
        return true;
    }
}
