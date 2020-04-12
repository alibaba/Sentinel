package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.DegradeRuleService;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.DeleteDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.degrade.QueryDegradeRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cdfive
 */
@Service
public class DegradeRuleServiceImpl extends BaseRuleService<DegradeRuleEntity> implements DegradeRuleService {


    @Override
    public List<QueryDegradeRuleListRespVo> queryDegradeRuleList(MachineReqVo reqVo) throws Exception {
        return null;
    }

    @Override
    public void addDegradeRule(AddFlowRuleReqVo reqVo) throws Exception {

    }

    @Override
    public void updateDegradeRule(UpdateDegradeRuleReqVo reqVo) throws Exception {

    }

    @Override
    public void deleteDegradeRule(DeleteDegradeRuleReqVo reqVo) throws Exception {

    }
}
