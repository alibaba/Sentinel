package com.alibaba.csp.sentinel.dashboard.repository;

import com.alibaba.csp.sentinel.dashboard.entity.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.entity.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
public abstract class AbstractRulePublisher<T extends RuleEntity> implements DynamicRulePublisher<T> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppManagement appManagement;

    @Autowired
    private Converter<List<T>, String> converter;

    @Autowired
    private RuleKeyBuilder<T> ruleKeyBuilder;

    @Override
    public void publish(String app, List<T> rules) throws Exception {
        if (StringUtil.isBlank(app)) {
            return;
        }

        if (rules == null) {
            return;
        }

        List<MachineInfo> machineInfos = appManagement.getDetailApp(app).getMachines()
                .stream()
                .filter(MachineInfo::isHealthy)
                .sorted((e1, e2) -> Long.compare(e2.getLastHeartbeat(), e1.getLastHeartbeat())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(machineInfos)) {
            return;
        }


        for (MachineInfo machineInfo : machineInfos) {
            this.publish(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), rules);
        }
    }

    @Override
    public void publish(String app, String ip, Integer port, List<T> rules) throws Exception {
        String rulesStr = converter.convert(rules);
//        String ruleKey = buildRuleKey(app, ip, port);
        publishRules(app, ip, port, rulesStr);
    }

    protected String buildRuleKey(String app, String ip, Integer port) {
//        return Joiner.on("-").join(app, ip, port, "flow", "rules");
        return ruleKeyBuilder.buildRuleKey(app, ip, port);
    }

    protected abstract void publishRules(String app, String ip, Integer port, String rules) throws Exception;
}
