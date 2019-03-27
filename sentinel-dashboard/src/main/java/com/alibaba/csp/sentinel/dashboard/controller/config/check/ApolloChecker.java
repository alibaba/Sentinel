package com.alibaba.csp.sentinel.dashboard.controller.config.check;

import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(ApolloChecker.class);

    @Autowired
    private AppManagement appManagement;

    @Override
    public boolean checkOperator(String operator, String app, String ip, Integer port) {
        if (Objects.isNull(operator)) {
            logger.warn("The operator is null, can't update rules");
            return false;
        }
        Optional<MachineInfo> machineInfoOptional = appManagement.getDetailApp(app).getMachine(ip, port);
        ApolloMachineInfo apolloMachineInfo = (ApolloMachineInfo) machineInfoOptional.get();
        AssertUtil.notNull(apolloMachineInfo, String.format("There is no equivalent machineInfo for app: %s, ip: %s, port: %s", app, ip, port));
        boolean result = true;
        if (operator.equals(apolloMachineInfo.getOperator())) {
            logger.info("it is sentinel dashboard modifications, no synchronization required, record operator:{}, request operator:{}",
                        apolloMachineInfo.getOperator(), operator);
            result = false;
        }
        return result;
    }
}
