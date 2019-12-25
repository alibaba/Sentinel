package com.alibaba.csp.sentinel.cluster.redis.request;

public class RequestData {
    private long flowId;
    private int acquireCount;

    public long getFlowId() {
        return flowId;
    }

    public void setFlowId(long flowId) {
        this.flowId = flowId;
    }

    public int getAcquireCount() {
        return acquireCount;
    }

    public void setAcquireCount(int acquireCount) {
        this.acquireCount = acquireCount;
    }

    @Override
    public String toString() {
        return "RequestData{" +
                "flowId=" + flowId +
                ", acquireCount=" + acquireCount +
                '}';
    }
}
