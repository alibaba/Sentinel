/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.event.listener;

import com.alibaba.csp.sentinel.event.SentinelEventListener;
import com.alibaba.csp.sentinel.event.exporter.EventExporter;
import com.alibaba.csp.sentinel.event.model.ParamFlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.model.impl.CircuitBreakerStateEvent;
import com.alibaba.csp.sentinel.event.model.impl.ClusterFallbackEvent;
import com.alibaba.csp.sentinel.event.model.impl.ClusterFlowStateEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.AuthorityBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.FlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.SystemBlockEvent;
import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Used to listen to the export events of Sentinel.
 *
 * @author Daydreamer-ia
 */
public class ParamFlowExportListener extends SentinelEventListener {

    @Override
    public void onEvent(SentinelEvent event) {
        String simpleName = event.getClass().getSimpleName();
        String jsonString = JSON.toJSONString(event);
        EventExporter.getINSTANCE().writeBuffer(simpleName + "|" + jsonString);
    }

    @Override
    public List<Class<? extends SentinelEvent>> eventType() {
        return Collections.singletonList(ParamFlowBlockEvent.class);
    }

}
