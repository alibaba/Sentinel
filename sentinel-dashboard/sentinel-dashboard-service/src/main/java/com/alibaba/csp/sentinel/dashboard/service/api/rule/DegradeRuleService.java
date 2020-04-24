package com.alibaba.csp.sentinel.dashboard.service.api.rule;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.AddDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.DeleteDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.degrade.QueryDegradeRuleListRespVo;

import java.util.List;

/**
 * @author cdfive
 */
public interface DegradeRuleService {

    List<QueryDegradeRuleListRespVo> queryDegradeRuleList(MachineReqVo reqVo) throws Exception;

    void addDegradeRule(AddDegradeRuleReqVo reqVo) throws Exception;

    void updateDegradeRule(UpdateDegradeRuleReqVo reqVo) throws Exception;

    void deleteDegradeRule(DeleteDegradeRuleReqVo reqVo) throws Exception;
}
