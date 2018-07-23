/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.property.SimplePropertyListener;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

/**
 * @author jialiang.linjl
 */
public class SampleCountProperty {

    public static volatile int sampleCount = 2;

    public static void init(SentinelProperty<Integer> property) {

        try {
            property.addListener(new SimplePropertyListener<Integer>() {
                @Override
                public void configUpdate(Integer value) {
                    if (value != null) {
                        sampleCount = value;
                        // Reset the value.
                        for (ClusterNode node : ClusterBuilderSlot.getClusterNodeMap().values()) {
                            node.reset();
                        }
                    }
                    RecordLog.info("Current SampleCount: " + sampleCount);
                }

            });
        } catch (Exception e) {
            RecordLog.info(e.getMessage(), e);
        }
    }
}
