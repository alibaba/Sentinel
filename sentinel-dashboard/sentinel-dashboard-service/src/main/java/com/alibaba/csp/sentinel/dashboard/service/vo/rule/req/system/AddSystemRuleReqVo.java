package com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;

/**
 * @author cdfive
 */
public class AddSystemRuleReqVo extends MachineReqVo {

    private Double highestSystemLoad;

    private Long avgRt;

    private Long maxThread;

    private Double qps;

    private Double highestCpuUsage;

    public Double getHighestSystemLoad() {
        return highestSystemLoad;
    }

    public void setHighestSystemLoad(Double highestSystemLoad) {
        this.highestSystemLoad = highestSystemLoad;
    }

    public Long getAvgRt() {
        return avgRt;
    }

    public void setAvgRt(Long avgRt) {
        this.avgRt = avgRt;
    }

    public Long getMaxThread() {
        return maxThread;
    }

    public void setMaxThread(Long maxThread) {
        this.maxThread = maxThread;
    }

    public Double getQps() {
        return qps;
    }

    public void setQps(Double qps) {
        this.qps = qps;
    }

    public Double getHighestCpuUsage() {
        return highestCpuUsage;
    }

    public void setHighestCpuUsage(Double highestCpuUsage) {
        this.highestCpuUsage = highestCpuUsage;
    }
}
