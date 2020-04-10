package com.alibaba.csp.sentinel.dashboard.web.vo.req.rule.degrade;

import com.alibaba.csp.sentinel.dashboard.web.vo.req.MachineReqVo;

/**
 * @author cdfive
 */
public class UpdateDegradeRuleReqVo extends MachineReqVo {

    /**降级策略 0-RT 1-异常比例 2-异常数*/
    private Integer grade;

    /**阈值*/
    private Double count;

    /**降级时间窗口,单位:秒*/
    private Integer timeWindow;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Integer getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Integer timeWindow) {
        this.timeWindow = timeWindow;
    }
}
