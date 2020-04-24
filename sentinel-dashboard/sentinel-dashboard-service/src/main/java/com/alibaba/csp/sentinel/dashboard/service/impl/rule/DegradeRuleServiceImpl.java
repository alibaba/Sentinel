package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.DegradeRuleService;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker.DegradeRuleVoChecker;
import com.alibaba.csp.sentinel.dashboard.service.impl.rule.convertor.DegradeRuleVoConvertor;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.AddDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.DeleteDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.degrade.QueryDegradeRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdfive
 */
@Service
public class DegradeRuleServiceImpl extends BaseRuleService<DegradeRuleEntity> implements DegradeRuleService {

    @Override
    public List<QueryDegradeRuleListRespVo> queryDegradeRuleList(MachineReqVo reqVo) throws Exception {
        List<DegradeRuleEntity> rules = queryRuleList(reqVo);
        return rules.stream().map(o -> DegradeRuleVoConvertor.convertList(o)).collect(Collectors.toList());
    }

    @Override
    public void addDegradeRule(AddDegradeRuleReqVo reqVo) throws Exception {
        DegradeRuleVoChecker.checkAdd(reqVo);
        DegradeRuleEntity rule = DegradeRuleVoConvertor.convertAdd(reqVo);
        addRule(reqVo, rule);
    }

    @Override
    public void updateDegradeRule(UpdateDegradeRuleReqVo reqVo) throws Exception {
        DegradeRuleVoChecker.checkUpdate(reqVo);
        updateRule(reqVo, reqVo.getId(), toUpdateRule -> DegradeRuleVoConvertor.convertUpdate(reqVo, toUpdateRule));
    }

    @Override
    public void deleteDegradeRule(DeleteDegradeRuleReqVo reqVo) throws Exception {
        DegradeRuleVoChecker.checkDelete(reqVo);
        deleteRule(reqVo, reqVo.getId());
    }
}
