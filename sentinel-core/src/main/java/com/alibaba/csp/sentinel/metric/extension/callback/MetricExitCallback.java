package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionInit;
import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * Metric extension exit callback.
 *
 * @author Carpenter Lee
 */
public class MetricExitCallback implements ProcessorSlotExitCallback {
    @Override
    public void onExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        for (MetricExtension m : MetricExtensionInit.getMetricExtensions()) {
            if (context.getCurEntry().getError() == null) {
                long realRt = TimeUtil.currentTimeMillis() - context.getCurEntry().getCreateTime();
                m.addRt(resourceWrapper.getName(), realRt);
                m.addSuccess(resourceWrapper.getName(), count);
                m.decreaseThreadNum(resourceWrapper.getName());
            }
        }
    }
}
