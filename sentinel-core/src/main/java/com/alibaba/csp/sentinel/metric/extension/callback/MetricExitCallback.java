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
            if (context.getCurEntry().getBlockError() != null) {
                continue;
            }
            String resource = resourceWrapper.getName();
            long realRt = TimeUtil.currentTimeMillis() - context.getCurEntry().getCreateTimestamp();
            m.addRt(resource, realRt, args);
            m.addSuccess(resource, count, args);
            m.decreaseThreadNum(resource, args);

            Throwable ex = context.getCurEntry().getError();
            if (ex != null) {
                m.addException(resource, count, ex);
            }
        }
    }
}
