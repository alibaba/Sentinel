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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;
import com.alibaba.csp.sentinel.cluster.request.data.BatchParamFlowRequestData;

import io.netty.buffer.ByteBuf;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public class BatchParamFlowRequestDataDecoder implements EntityDecoder<ByteBuf, BatchParamFlowRequestData> {

    @Override
    public BatchParamFlowRequestData decode(ByteBuf source) {
        if (source.readableBytes() >= 4) {
            int batchSize = source.readInt();
            if (batchSize <= 0) {
                return null;
            }
            Set<Long> idSet = new HashSet<>();
            for (int i = 0; i < batchSize; i++) {
                idSet.add(source.readLong());
            }

            BatchParamFlowRequestData requestData = new BatchParamFlowRequestData()
                .setFlowIds(idSet)
                .setCount(source.readInt());

            Map<Integer, Object> paramMap = ParamDecodeUtils.decodeParamMap(source);
            requestData.setParamMap(paramMap);
            return requestData;
        }
        return null;
    }

}
