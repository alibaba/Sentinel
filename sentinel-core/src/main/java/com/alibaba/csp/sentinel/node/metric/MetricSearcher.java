package com.alibaba.csp.sentinel.node.metric;

import java.util.List;

public interface MetricSearcher {

    List<MetricNode> find(long beginTimeMs, int recommendLines) throws Exception;

    List<MetricNode> findByTimeAndResource(long beginTimeMs, long endTimeMs, String identity)
        throws Exception;
}
