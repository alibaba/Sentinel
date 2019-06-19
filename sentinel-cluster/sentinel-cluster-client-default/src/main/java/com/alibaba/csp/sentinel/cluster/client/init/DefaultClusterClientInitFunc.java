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
package com.alibaba.csp.sentinel.cluster.client.init;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.client.ClientConstants;
import com.alibaba.csp.sentinel.cluster.client.codec.data.BatchFlowRequestDataWriter;
import com.alibaba.csp.sentinel.cluster.client.codec.data.BatchFlowResponseDataDecoder;
import com.alibaba.csp.sentinel.cluster.client.codec.data.BatchParamFlowRequestDataWriter;
import com.alibaba.csp.sentinel.cluster.client.codec.data.FlowRequestDataWriter;
import com.alibaba.csp.sentinel.cluster.client.codec.data.FlowResponseDataDecoder;
import com.alibaba.csp.sentinel.cluster.client.codec.data.ParamFlowRequestDataWriter;
import com.alibaba.csp.sentinel.cluster.client.codec.data.PingRequestDataWriter;
import com.alibaba.csp.sentinel.cluster.client.codec.data.PingResponseDataDecoder;
import com.alibaba.csp.sentinel.cluster.client.codec.registry.RequestDataWriterRegistry;
import com.alibaba.csp.sentinel.cluster.client.codec.registry.ResponseDataDecodeRegistry;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientGlobalConfig;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.init.InitOrder;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@InitOrder(0)
public class DefaultClusterClientInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        initDefaultEntityWriters();
        initDefaultEntityDecoders();
    }

    private void initDefaultEntityWriters() {
        RequestDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_PING, new PingRequestDataWriter());
        RequestDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_FLOW, new FlowRequestDataWriter());
        RequestDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_BATCH_FLOW, new BatchFlowRequestDataWriter());

        Integer maxParamByteSize = ClusterClientGlobalConfig.getMaxParamByteSize();
        if (maxParamByteSize == null) {
            maxParamByteSize = ClusterClientGlobalConfig.DEFAULT_PARAM_MAX_SIZE;
        }
        RequestDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_PARAM_FLOW,
            new ParamFlowRequestDataWriter(maxParamByteSize));
        RequestDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_BATCH_PARAM_FLOW,
            new BatchParamFlowRequestDataWriter(maxParamByteSize));
    }

    private void initDefaultEntityDecoders() {
        ResponseDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_PING, new PingResponseDataDecoder());
        ResponseDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_FLOW, new FlowResponseDataDecoder());
        ResponseDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_PARAM_FLOW, new FlowResponseDataDecoder());
        ResponseDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_BATCH_FLOW,
            new BatchFlowResponseDataDecoder());
        ResponseDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_BATCH_PARAM_FLOW,
            new BatchFlowResponseDataDecoder());
    }
}
