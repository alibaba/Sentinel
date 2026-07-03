package com.alibaba.csp.sentinel.slots.block.flow.param;

class TokenUpdateStatus {

    private final long lastAddTokenTime;

    private final long restQps;

    public TokenUpdateStatus(long lastAddTokenTime, long restQps) {
        this.lastAddTokenTime = lastAddTokenTime;
        this.restQps = restQps;
    }

    public long getLastAddTokenTime() {
        return lastAddTokenTime;
    }

    public long getRestQps() {
        return restQps;
    }

    @Override
    public String toString() {
        return "TokenUpdateStatus{" +
                "hash=" + System.identityHashCode(this) +
                ", lastAddTokenTime=" + lastAddTokenTime +
                ", requestCount=" + restQps +
                '}';
    }
}
