package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
public abstract class AbstractRuleProvider<T> implements DynamicRuleProvider<T> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppManagement appManagement;

    @Autowired
    private Converter<String, List<T>> converter;

    @Autowired
    private RuleKeyBuilder<T> ruleKeyBuilder;

    @Override
    public List<T> getRules(String app) throws Exception{
        if (StringUtil.isBlank(app)) {
            return new ArrayList<>();
        }

        List<MachineInfo> machineInfos = appManagement.getDetailApp(app).getMachines()
                .stream()
                .filter(MachineInfo::isHealthy)
                .sorted((e1, e2) -> Long.compare(e2.getLastHeartbeat(), e1.getLastHeartbeat())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(machineInfos)) {
            return new ArrayList<>();
        } else {
            MachineInfo machine = machineInfos.get(0);
            return this.getRules(machine.getApp(), machine.getIp(), machine.getPort());
        }
    }

    @Override
    public List<T> getRules(String app, String ip, Integer port) throws Exception {
//        String ruleKey = buildRuleKey(app, ip, port);
        String ruleStr = fetchRules(app, ip, port);
        if (StringUtil.isEmpty(ruleStr)) {
            return new ArrayList<>();
        }

        return converter.convert(ruleStr);
    }

    protected String buildRuleKey(String app, String ip, Integer port) {
//        return Joiner.on("-").join(app, ip, port, "flow", "rules");
        return ruleKeyBuilder.buildRuleKey(app, ip, port);
    }

    protected abstract String fetchRules(String app, String ip, Integer port) throws Exception;
}
