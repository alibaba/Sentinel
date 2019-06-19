/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.client.codec.data;

import java.util.Map;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientGlobalConfig;
import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;
import com.alibaba.csp.sentinel.cluster.request.data.BatchParamFlowRequestData;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;

import io.netty.buffer.ByteBuf;

/**
 * <p>The encoding format of the {@link BatchParamFlowRequestData}:</p>
 * <pre>
 * +-------------+----------------+-----------+
 * | Flow ID Set | Count(4 bytes) | Param Map |
 * +-------------+----------------+-----------+
 * </pre>
 *
 * <p>The layout of flow ID set:</p>
 * <pre>
 * +-----------------+------------------------------+
 * | Amount(4 bytes) | repeated: FlowId(8 bytes)... |
 * +-----------------+------------------------------+
 * </pre>
 *
 * <p>The layout of the parameter map:</p>
 * <pre>
 * +----------------------+------------------------+
 * | ParamAmount(4 bytes) | repeated: ParamItem... |
 * +----------------------+------------------------+
 * </pre>
 *
 * <p>The layout of a single parameter item (excluding String):</p>
 * <pre>
 * +-------------------+-------------------+----------+
 * | ParamIdx(4 bytes) | ParamType(1 byte) | ParamObj |
 * +-------------------+-------------------+----------+
 * </pre>
 *
 * <p>The layout of a single String item:</p>
 * <pre>
 * +-------------------+-------------------+-----------------+----------+
 * | ParamIdx(4 bytes) | ParamType(1 byte) | StrLen(4 bytes) | StrBytes |
 * +-------------------+-------------------+-----------------+----------+
 * </pre>
 *
 * @author Eric Zhao
 * @since 1.7.0
 */
public class BatchParamFlowRequestDataWriter implements EntityWriter<BatchParamFlowRequestData, ByteBuf> {

    private final ParamRequestEncoder encoder;

    public BatchParamFlowRequestDataWriter() {
        this(ClusterClientGlobalConfig.DEFAULT_PARAM_MAX_SIZE);
    }

    public BatchParamFlowRequestDataWriter(int maxParamByteSize) {
        AssertUtil.isTrue(maxParamByteSize > 0, "maxParamByteSize should be positive");
        this.encoder = new ParamRequestEncoder(maxParamByteSize);
    }

    @Override
    public void writeTo(BatchParamFlowRequestData entity, ByteBuf target) {
        if (entity.getFlowIds() == null || entity.getFlowIds().isEmpty()) {
            RecordLog.warn("[BatchParamFlowRequestDataWriter] Flow ID set is empty, ignoring the request");
            return;
        }
        int ruleAmount = entity.getFlowIds().size();
        // Write the amount of rule IDs.
        target.writeInt(ruleAmount);
        for (Long id : entity.getFlowIds()) {
            target.writeLong(id);
        }

        target.writeInt(entity.getCount());

        Map<Integer, Object> params = encoder.resolveValidParamMap(entity.getParamMap());
        // Write the amount of the parameter list.
        target.writeInt(params.size());

        // Serialize parameters with type flag.
        for (Map.Entry<Integer, Object> e : params.entrySet()) {
            // Write paramIdx.
            target.writeInt(e.getKey());
            encoder.encodeValue(e.getValue(), target);
        }
    }
}
