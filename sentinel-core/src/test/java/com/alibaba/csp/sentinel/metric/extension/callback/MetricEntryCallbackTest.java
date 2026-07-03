/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        FakeAdvancedMetricExtension advancedExtension = new FakeAdvancedMetricExtension();
        MetricExtensionProvider.addMetricExtension(extension);
        MetricExtensionProvider.addMetricExtension(advancedExtension);

        MetricEntryCallback entryCallback = new MetricEntryCallback();
        StringResourceWrapper resourceWrapper = new StringResourceWrapper("resource", EntryType.OUT);
        int count = 2;
        Object[] args = {"args1", "args2"};
        entryCallback.onPass(null, resourceWrapper, null, count, args);
        // assert extension
        Assert.assertEquals(extension.pass, count);
        Assert.assertEquals(extension.thread, 1);
        
        // assert advancedExtension
        Assert.assertEquals(advancedExtension.pass, count);
        Assert.assertEquals(advancedExtension.concurrency, 1);
    }

    @Test
    public void onBlocked() throws Exception {
        FakeMetricExtension extension = new FakeMetricExtension();
        FakeAdvancedMetricExtension advancedExtension = new FakeAdvancedMetricExtension();
        MetricExtensionProvider.addMetricExtension(extension);
        MetricExtensionProvider.addMetricExtension(advancedExtension);

        MetricEntryCallback entryCallback = new MetricEntryCallback();
        StringResourceWrapper resourceWrapper = new StringResourceWrapper("resource", EntryType.OUT);
        Context context = mock(Context.class);
        when(context.getOrigin()).thenReturn("origin1");
        int count = 2;
        Object[] args = {"args1", "args2"};
        entryCallback.onBlocked(new FlowException("xx"), context, resourceWrapper, null, count, args);
        // assert extension
        Assert.assertEquals(extension.block, count);
        // assert advancedExtension
        Assert.assertEquals(advancedExtension.block, count);
    }
}