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
package com.alibaba.csp.sentinel.cluster.server.codec;

import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;
import com.alibaba.csp.sentinel.cluster.codec.request.RequestEntityDecoder;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.server.codec.registry.RequestDataDecodeRegistry;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.buffer.ByteBuf;

/**
 * <p>Default entity decoder for any {@link ClusterRequest} entity.</p>
 *
 * <p>Decode format:</p>
 * <pre>
 * +--------+---------+---------+
 * | xid(4) | type(1) | data... |
 * +--------+---------+---------+
 * </pre>
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class DefaultRequestEntityDecoder implements RequestEntityDecoder<ByteBuf, ClusterRequest> {

    @Override
    public ClusterRequest decode(ByteBuf source) {
        if (source.readableBytes() >= 5) {
            int xid = source.readInt();
            int type = source.readByte();

            EntityDecoder<ByteBuf, ?> dataDecoder = RequestDataDecodeRegistry.getDecoder(type);
            if (dataDecoder == null) {
                RecordLog.warn("Unknown type of request data decoder: {}", type);
                return null;
            }

            Object data;
            if (source.readableBytes() == 0) {
                data = null;
            } else {
                data = dataDecoder.decode(source);
            }

            return new ClusterRequest<>(xid, type, data);
        }
        return null;
    }
}
