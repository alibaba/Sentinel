package com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;

/**
 * @author cdfive
 */
public class DeleteDegradeRuleReqVo extends MachineReqVo {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
