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

import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;
import com.alibaba.csp.sentinel.cluster.response.data.BatchFlowTokenResponseData;

import io.netty.buffer.ByteBuf;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public class BatchFlowResponseDataDecoder implements EntityDecoder<ByteBuf, BatchFlowTokenResponseData> {

    @Override
    public BatchFlowTokenResponseData decode(ByteBuf source) {
        BatchFlowTokenResponseData data = new BatchFlowTokenResponseData();

        if (source.readableBytes() >= 4) {
            data.setWaitInMs(source.readInt());
            if (source.readableBytes() == 8) {
                data.setBlockId(source.readLong());
            }
        }
        return data;
    }
}
