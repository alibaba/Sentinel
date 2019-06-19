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
package com.alibaba.csp.sentinel.cluster.client.codec.data;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientGlobalConfig;
import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;
import com.alibaba.csp.sentinel.cluster.request.data.ParamFlowRequestData;
import com.alibaba.csp.sentinel.util.AssertUtil;

import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ParamFlowRequestDataWriter implements EntityWriter<ParamFlowRequestData, ByteBuf> {

    private final ParamRequestEncoder encoder;

    public ParamFlowRequestDataWriter() {
        this(ClusterClientGlobalConfig.DEFAULT_PARAM_MAX_SIZE);
    }

    public ParamFlowRequestDataWriter(int maxParamByteSize) {
        AssertUtil.isTrue(maxParamByteSize > 0, "maxParamByteSize should be positive");
        this.encoder = new ParamRequestEncoder(maxParamByteSize);
    }

    @Override
    public void writeTo(ParamFlowRequestData entity, ByteBuf target) {
        target.writeLong(entity.getFlowId());
        target.writeInt(entity.getCount());

        List<Object> params = encoder.resolveValidParams(entity.getParams());
        // Write the amount of the parameter list.
        target.writeInt(params.size());

        // Serialize parameters with type flag.
        for (Object param : params) {
            encoder.encodeValue(param, target);
        }
    }
}
