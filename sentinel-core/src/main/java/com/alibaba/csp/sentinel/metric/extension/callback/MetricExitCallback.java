package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * Metric extension exit callback.
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public class MetricExitCallback implements ProcessorSlotExitCallback {
    @Override
    public void onExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        for (MetricExtension m : MetricExtensionProvider.getMetricExtensions()) {
            if (context.getCurEntry().getError() == null) {
                long realRt = TimeUtil.currentTimeMillis() - context.getCurEntry().getCreateTime();
                m.addRt(resourceWrapper.getName(), realRt, args);
                m.addSuccess(resourceWrapper.getName(), count, args);
                m.decreaseThreadNum(resourceWrapper.getName(), args);
            }
        }
    }
}
