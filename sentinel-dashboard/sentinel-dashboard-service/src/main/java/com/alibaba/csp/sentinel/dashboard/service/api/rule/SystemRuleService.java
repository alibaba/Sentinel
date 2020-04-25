package com.alibaba.csp.sentinel.dashboard.service.api.rule;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.DeleteSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.system.QuerySystemRuleListRespVo;

import java.util.List;

/**
 * @author cdfive
 */
public interface SystemRuleService {

    List<QuerySystemRuleListRespVo> querySystemRuleList(MachineReqVo reqVo) throws Exception;

    void addSystemRule(AddSystemRuleReqVo reqVo) throws Exception;

    void updateSystemRule(UpdateSystemRuleReqVo reqVo) throws Exception;

    void deleteSystemRule(DeleteSystemRuleReqVo reqVo) throws Exception;
}
