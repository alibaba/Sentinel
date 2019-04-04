package com.alibaba.csp.sentinel.dashboard.controller.config.check;

import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * do some check if use Apollo as DataSource
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloChecker implements Checker {

    @Autowired
    private AppManagement appManagement;

    @Override
    public boolean checkOperator(String operator, String app, String ip, Integer port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                       .filter(appInfo -> Objects.nonNull(operator))
                       .flatMap(appInfo -> appInfo.getMachine(ip, port))
                       .map(machineInfo -> !operator.equals(((ApolloMachineInfo) machineInfo).getOperator()))
                       .orElse(false);
    }
}
