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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.metric.extension.AdvancedMetricExtension;
import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Metric extension entry callback.
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public class MetricEntryCallback implements ProcessorSlotEntryCallback<DefaultNode> {

    @Override
    public void onPass(Context context, ResourceWrapper rw, DefaultNode param, int count, Object... args)
        throws Exception {
        for (MetricExtension m : MetricExtensionProvider.getMetricExtensions()) {
            if (m instanceof AdvancedMetricExtension) {
                ((AdvancedMetricExtension) m).onPass(rw, count, args);
            } else {
                m.increaseThreadNum(rw.getName(), args);
                m.addPass(rw.getName(), count, args);
            }
        }
    }

    @Override
    public void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, DefaultNode param,
                          int count, Object... args) {
        for (MetricExtension m : MetricExtensionProvider.getMetricExtensions()) {
            if (m instanceof AdvancedMetricExtension) {
                ((AdvancedMetricExtension) m).onBlocked(resourceWrapper, count, context.getOrigin(), ex, args);
            } else {
                m.addBlock(resourceWrapper.getName(), count, context.getOrigin(), ex, args);
            }
        }
    }
}
