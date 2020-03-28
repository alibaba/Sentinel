package com.alibaba.csp.sentinel.dashboard.controller.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.vo.req.MachineReqVo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author cdfive
 */
public class BaseRuleController<T extends RuleEntity> {

    @Autowired
    private DynamicRulePublisher<T> rulePublisher;

    @Autowired
    private InMemoryRuleRepositoryAdapter<T> repository;


    private void publishRules(/*@NonNull*/ MachineReqVo reqVo) throws Exception {
        boolean operateApp = isOperateApp(reqVo);
        if (operateApp) {
            List<T> rules = repository.findAllByApp(reqVo.getApp());
            rulePublisher.publish(reqVo.getApp(), rules);
        } else {
            List<T> rules = repository.findAllByMachine(MachineInfo.of(reqVo.getApp(), reqVo.getIp(), reqVo.getPort()));
            rulePublisher.publish(reqVo.getApp(), reqVo.getIp(), reqVo.getPort(), rules);
        }
    }

    protected boolean isOperateApp(MachineReqVo reqVo) {
        String ip = reqVo.getIp();
        Integer port = reqVo.getPort();
        return StringUtil.isEmpty(ip) || port == null;
    }
}
