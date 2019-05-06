package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Carpenter Lee
 */
public class MetricEntryCallbackTest {

    @Test
    public void onPass() throws Exception {
        FakeMetricExtension extension = new FakeMetricExtension();
        MetricExtensionProvider.addMetricExtension(extension);

        MetricEntryCallback entryCallback = new MetricEntryCallback();
        StringResourceWrapper resourceWrapper = new StringResourceWrapper("resource", EntryType.OUT);
        int count = 2;
        Object[] args = {"args1", "args2"};
        entryCallback.onPass(null, resourceWrapper, null, count, args);
        Assert.assertEquals(extension.pass, count);
        Assert.assertEquals(extension.thread, 1);
    }

    @Test
    public void onBlocked() throws Exception {
        FakeMetricExtension extension = new FakeMetricExtension();
        MetricExtensionProvider.addMetricExtension(extension);

        MetricEntryCallback entryCallback = new MetricEntryCallback();
        StringResourceWrapper resourceWrapper = new StringResourceWrapper("resource", EntryType.OUT);
        Context context = mock(Context.class);
        when(context.getOrigin()).thenReturn("origin1");
        int count = 2;
        Object[] args = {"args1", "args2"};
        entryCallback.onBlocked(new FlowException("xx"), context, resourceWrapper, null, count, args);
        Assert.assertEquals(extension.block, count);
    }
}