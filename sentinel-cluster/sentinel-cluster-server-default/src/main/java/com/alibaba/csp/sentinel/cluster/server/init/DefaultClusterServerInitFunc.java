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
package com.alibaba.csp.sentinel.cluster.server.init;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.server.TokenServiceProvider;
import com.alibaba.csp.sentinel.cluster.server.codec.data.FlowRequestDataDecoder;
import com.alibaba.csp.sentinel.cluster.server.codec.data.FlowResponseDataWriter;
import com.alibaba.csp.sentinel.cluster.server.codec.data.ParamFlowRequestDataDecoder;
import com.alibaba.csp.sentinel.cluster.server.codec.data.PingRequestDataDecoder;
import com.alibaba.csp.sentinel.cluster.server.codec.data.PingResponseDataWriter;
import com.alibaba.csp.sentinel.cluster.server.codec.registry.RequestDataDecodeRegistry;
import com.alibaba.csp.sentinel.cluster.server.codec.registry.ResponseDataWriterRegistry;
import com.alibaba.csp.sentinel.cluster.server.processor.RequestProcessorProvider;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class DefaultClusterServerInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        initDefaultEntityDecoders();
        initDefaultEntityWriters();

        initDefaultProcessors();

        // Eagerly-trigger the SPI pre-load of token service.
        TokenServiceProvider.getService();

        RecordLog.info("[DefaultClusterServerInitFunc] Default entity codec and processors registered");
    }

    private void initDefaultEntityWriters() {
        ResponseDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_PING, new PingResponseDataWriter());
        ResponseDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_FLOW, new FlowResponseDataWriter());
        ResponseDataWriterRegistry.addWriter(ClusterConstants.MSG_TYPE_PARAM_FLOW, new FlowResponseDataWriter());
    }

    private void initDefaultEntityDecoders() {
        RequestDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_PING, new PingRequestDataDecoder());
        RequestDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_FLOW, new FlowRequestDataDecoder());
        RequestDataDecodeRegistry.addDecoder(ClusterConstants.MSG_TYPE_PARAM_FLOW, new ParamFlowRequestDataDecoder());
    }

    private void initDefaultProcessors() {
        // Eagerly-trigger the SPI pre-load.
        RequestProcessorProvider.getProcessor(0);
    }
}
