package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.SystemRuleService;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker.SystemRuleVoChecker;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.convertor.SystemRuleVoConvertor;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.DeleteSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.system.QuerySystemRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
@Service
public class SystemRuleServiceImpl extends BaseRuleService<SystemRuleEntity> implements SystemRuleService {

    @Override
    public List<QuerySystemRuleListRespVo> querySystemRuleList(MachineReqVo reqVo) throws Exception {
        List<SystemRuleEntity> rules = queryRuleList(reqVo);
        return rules.stream().map(o -> SystemRuleVoConvertor.convertList(o)).collect(Collectors.toList());
    }

    @Override
    public void addSystemRule(AddSystemRuleReqVo reqVo) throws Exception {
        SystemRuleVoChecker.checkAdd(reqVo);
        SystemRuleEntity rule = SystemRuleVoConvertor.convertAdd(reqVo);
        addRule(reqVo, rule);
    }

    @Override
    public void updateSystemRule(UpdateSystemRuleReqVo reqVo) throws Exception {
        SystemRuleVoChecker.checkUpdate(reqVo);
        updateRule(reqVo,reqVo.getId(), toUpdateRule -> SystemRuleVoConvertor.convertUpdate(reqVo, toUpdateRule));
    }

    @Override
    public void deleteSystemRule(DeleteSystemRuleReqVo reqVo) throws Exception {
        SystemRuleVoChecker.checkDelete(reqVo);
        deleteRule(reqVo, reqVo.getId());
    }
}
