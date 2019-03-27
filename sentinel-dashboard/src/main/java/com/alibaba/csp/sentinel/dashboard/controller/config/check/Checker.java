package com.alibaba.csp.sentinel.dashboard.controller.config.check;

/**
 * do some check
 *
 * @author longqiang
 */
public interface Checker {

    /**
     * Check operator caused the rule configuration change to use different countermeasures
     *
     * @param operator
     * @param app
     * @param ip
     * @param port
     * @return boolean
     */
    boolean checkOperator(String operator, String app, String ip, Integer port);

}
