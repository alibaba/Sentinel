package com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;

/**
 * @author cdfive
 */
public class AddDegradeRuleReqVo extends MachineReqVo {

    /**资源名称*/
    private String resource;

    /**降级策略 0-RT 1-异常比例 2-异常数*/
    private Integer grade;

    /**阈值*/
    private Double count;

    /**降级时间窗口,单位:秒*/
    private Integer timeWindow;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

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
