package com.alibaba.csp.sentinel.node.metric;

import java.util.List;

public interface MetricWriter {

    void write(long time, List<MetricNode> nodes) throws Exception;
}
