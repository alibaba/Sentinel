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
package com.alibaba.csp.sentinel.cluster.client.codec;

import com.alibaba.csp.sentinel.cluster.client.codec.registry.ResponseDataDecodeRegistry;
import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;
import com.alibaba.csp.sentinel.cluster.codec.response.ResponseEntityDecoder;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.buffer.ByteBuf;

/**
 * <p>Default entity decoder for any {@link ClusterResponse} entity.</p>
 *
 * <p>Decode format:</p>
 * <pre>
 * +--------+---------+-----------+---------+
 * | xid(4) | type(1) | status(1) | data... |
 * +--------+---------+-----------+---------+
 * </pre>
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class DefaultResponseEntityDecoder implements ResponseEntityDecoder<ByteBuf, ClusterResponse> {

    @Override
    public ClusterResponse decode(ByteBuf source) {
        if (source.readableBytes() >= 6) {
            int xid = source.readInt();
            int type = source.readByte();
            int status = source.readByte();

            EntityDecoder<ByteBuf, ?> decoder = ResponseDataDecodeRegistry.getDecoder(type);
            if (decoder == null) {
                RecordLog.warn("Unknown type of response data decoder: {}", type);
                return null;
            }

            Object data;
            if (source.readableBytes() == 0) {
                data = null;
            } else {
                data = decoder.decode(source);
            }

            return new ClusterResponse<>(xid, type, status, data);
        }
        return null;
    }
}
