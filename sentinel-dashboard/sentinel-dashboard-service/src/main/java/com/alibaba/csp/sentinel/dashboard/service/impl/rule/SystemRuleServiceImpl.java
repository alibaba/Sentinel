package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.SystemRuleService;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.DeleteSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.system.QuerySystemRuleListRespVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cdfive
 */
@Service
public class SystemRuleServiceImpl extends BaseRuleService<SystemRuleEntity> implements SystemRuleService {

    @Override
    public List<QuerySystemRuleListRespVo> querySystemRuleList(MachineReqVo reqVo) throws Exception {
        return null;
    }

    @Override
    public void addSystemRule(AddSystemRuleReqVo reqVo) throws Exception {

    }

    @Override
    public void updateSystemRule(UpdateSystemRuleReqVo reqVo) throws Exception {

    }

    @Override
    public void deleteSystemRule(DeleteSystemRuleReqVo reqVo) throws Exception {

    }
}
