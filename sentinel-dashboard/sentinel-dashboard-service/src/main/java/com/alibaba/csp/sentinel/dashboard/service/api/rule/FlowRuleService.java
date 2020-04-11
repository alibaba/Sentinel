package com.alibaba.csp.sentinel.dashboard.service.api.rule;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.DeleteFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.flow.QueryFlowRuleListRespVo;

import java.util.List;

/**
 * @author cdfive
 */
public interface FlowRuleService {

    List<QueryFlowRuleListRespVo> queryFlowRuleList(MachineReqVo reqVo) throws Exception;

    void addFlowRule(AddFlowRuleReqVo reqVo) throws Exception;

    void updateFlowRule(UpdateFlowRuleReqVo reqVo) throws Exception;

    void deleteFlowRule(DeleteFlowRuleReqVo reqVo) throws Exception;

}
