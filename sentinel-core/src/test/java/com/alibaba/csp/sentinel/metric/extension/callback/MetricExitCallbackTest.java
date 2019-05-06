package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Carpenter Lee
 */
public class MetricExitCallbackTest {

    @Test
    public void onExit() {
        FakeMetricExtension extension = new FakeMetricExtension();
        MetricExtensionProvider.addMetricExtension(extension);

        MetricExitCallback exitCallback = new MetricExitCallback();
        StringResourceWrapper resourceWrapper = new StringResourceWrapper("resource", EntryType.OUT);
        int count = 2;
        Object[] args = {"args1", "args2"};
        extension.rt = 20;
        extension.success = 6;
        extension.thread = 10;
        Context context = mock(Context.class);
        Entry entry = mock(Entry.class);
        when(entry.getError()).thenReturn(null);
        when(entry.getCreateTime()).thenReturn(TimeUtil.currentTimeMillis() - 100);
        when(context.getCurEntry()).thenReturn(entry);
        exitCallback.onExit(context, resourceWrapper, count, args);
        Assert.assertEquals(120, extension.rt, 10);
        Assert.assertEquals(extension.success, 6 + count);
        Assert.assertEquals(extension.thread, 10 - 1);
    }
}