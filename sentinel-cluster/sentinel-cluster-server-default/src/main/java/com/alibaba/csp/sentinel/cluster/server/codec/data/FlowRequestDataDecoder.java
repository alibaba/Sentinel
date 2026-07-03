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
package com.alibaba.csp.sentinel.cluster.server.codec.data;

import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;
import com.alibaba.csp.sentinel.cluster.request.data.FlowRequestData;

import io.netty.buffer.ByteBuf;

/**
 * <p>
 * Decoder for {@link FlowRequestData} from {@code ByteBuf} stream. The layout:
 * </p>
 * <pre>
 * | flow ID (8) | count (4) | priority flag (1) |
 * </pre>
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class FlowRequestDataDecoder implements EntityDecoder<ByteBuf, FlowRequestData> {

    @Override
    public FlowRequestData decode(ByteBuf source) {
        if (source.readableBytes() >= 12) {
            FlowRequestData requestData = new FlowRequestData()
                .setFlowId(source.readLong())
                .setCount(source.readInt());
            if (source.readableBytes() >= 1) {
                requestData.setPriority(source.readBoolean());
            }
            return requestData;
        }
        return null;
    }
}
