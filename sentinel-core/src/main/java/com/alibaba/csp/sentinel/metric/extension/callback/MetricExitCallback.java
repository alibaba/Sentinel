package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.metric.extension.AdvancedMetricExtension;
import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
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
			String entryType = resourceWrapper.getEntryType().name();
			Throwable ex = context.getCurEntry().getError();
			long realRt = TimeUtil.currentTimeMillis() - context.getCurEntry().getCreateTimestamp();
			if (m instanceof AdvancedMetricExtension) {
				((AdvancedMetricExtension) m).addRt(resource, entryType, realRt, args);
				((AdvancedMetricExtension) m).addSuccess(resource, entryType, count, args);
				((AdvancedMetricExtension) m).decreaseThreadNum(resource, entryType, args);
				if (null != ex) {
					((AdvancedMetricExtension) m).addException(resource, entryType, count, ex);
				}
			} else {
				m.addRt(resource, realRt, args);
				m.addSuccess(resource, count, args);
				m.decreaseThreadNum(resource, args);
				if (null != ex) {
					m.addException(resource, count, ex);
				}
			}
		}
	}
}
