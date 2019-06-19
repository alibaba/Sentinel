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
package com.alibaba.csp.sentinel.cluster.server.codec.data;

import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;
import com.alibaba.csp.sentinel.cluster.response.data.BatchFlowTokenResponseData;

import io.netty.buffer.ByteBuf;

/**
 * <p>The encoding format of the batch flow response data:</p>
 * <pre>
 * +--------------------+-------------------+----------------------------+
 * | Remaining(4 bytes) | WaitTime(4 bytes) | BlockId(8 bytes, optional) |
 * +--------------------+-------------------+----------------------------+
 *
 * @author Eric Zhao
 * @since 1.7.0
 */
public class BatchFlowResponseDataWriter implements EntityWriter<BatchFlowTokenResponseData, ByteBuf> {

    @Override
    public void writeTo(BatchFlowTokenResponseData entity, ByteBuf out) {
        if (entity == null) {
            return;
        }
        out.writeInt(entity.getWaitInMs());
        if (entity.getBlockId() != null) {
            out.writeLong(entity.getBlockId());
        }
    }
}
