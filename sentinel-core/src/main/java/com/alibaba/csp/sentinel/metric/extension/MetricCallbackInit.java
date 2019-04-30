package com.alibaba.csp.sentinel.metric.extension;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.metric.extension.callback.MetricEntryCallback;
import com.alibaba.csp.sentinel.metric.extension.callback.MetricExitCallback;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry;

/**
 * Register callbacks for metric extension.
 *
 * @author Carpenter Lee
 */
public class MetricCallbackInit implements InitFunc {
    @Override
    public void init() throws Exception {
        StatisticSlotCallbackRegistry.addEntryCallback(MetricEntryCallback.class.getCanonicalName(),
            new MetricEntryCallback());
        StatisticSlotCallbackRegistry.addExitCallback(MetricExitCallback.class.getCanonicalName(),
            new MetricExitCallback());
    }
}
