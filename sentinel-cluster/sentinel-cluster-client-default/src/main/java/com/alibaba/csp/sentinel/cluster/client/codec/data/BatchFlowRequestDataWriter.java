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

import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;
import com.alibaba.csp.sentinel.cluster.request.data.BatchFlowRequestData;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.buffer.ByteBuf;

/**
 * <p>The encoding format of the batch request:</p>
 * <pre>
 * +----------------+---------------+------------------+
 * |  Flow ID Set   | Count(4 byte) | PriorityFlag (1) |
 * +----------------+---------------+------------------+
 * </pre>
 * <p>The encoding format of flow ID set:</p>
 * <pre>
 * +----------------+-----------------------------+
 * | Amount(4 byte) | repeated: FlowId(8 byte)... |
 * +----------------+-----------------------------+
 * </pre>
 *
 * @author Eric Zhao
 * @since 1.7.0
 */
public class BatchFlowRequestDataWriter implements EntityWriter<BatchFlowRequestData, ByteBuf> {

    @Override
    public void writeTo(BatchFlowRequestData entity, ByteBuf target) {
        if (entity.getFlowIds() == null || entity.getFlowIds().isEmpty()) {
            RecordLog.warn("[BatchFlowRequestDataWriter] Flow ID set is empty, ignoring the request");
            return;
        }
        int ruleAmount = entity.getFlowIds().size();
        // Write the amount.
        target.writeInt(ruleAmount);
        for (Long id : entity.getFlowIds()) {
            target.writeLong(id);
        }

        target.writeInt(entity.getCount());
        target.writeBoolean(entity.isPriority());
    }
}
