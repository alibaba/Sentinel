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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;
import com.alibaba.csp.sentinel.cluster.request.data.ParamFlowRequestData;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ParamFlowRequestDataDecoder implements EntityDecoder<ByteBuf, ParamFlowRequestData> {

    private static final int REQUEST_HEADER_LENGTH = Long.BYTES + Integer.BYTES + Integer.BYTES;
    private static final int MIN_PARAM_LENGTH = Byte.BYTES + Byte.BYTES;

    @Override
    public ParamFlowRequestData decode(ByteBuf source) {
        ensureReadable(source, REQUEST_HEADER_LENGTH);

        ParamFlowRequestData requestData = new ParamFlowRequestData()
                .setFlowId(source.readLong())
                .setCount(source.readInt());

        int amount = source.readInt();
        if (amount < 0 || amount > ServerConstants.MAX_PARAM_AMOUNT
            || amount > source.readableBytes() / MIN_PARAM_LENGTH) {
            throw new CorruptedFrameException("Invalid parameter amount: " + amount);
        }

        List<Object> params;
        if (amount == 0) {
            params = Collections.emptyList();
        } else {
            params = new ArrayList<>(Math.min(amount, 16));
            for (int i = 0; i < amount; i++) {
                decodeParam(source, params);
            }
        }
        if (source.isReadable()) {
            throw new CorruptedFrameException("Parameter flow payload contains trailing bytes: "
                + source.readableBytes());
        }
        return requestData.setParams(params);
    }

    private void decodeParam(ByteBuf source, List<Object> params) {
        ensureReadable(source, Byte.BYTES);
        byte paramType = source.readByte();

        switch (paramType) {
            case ClusterConstants.PARAM_TYPE_INTEGER:
                ensureReadable(source, Integer.BYTES);
                params.add(source.readInt());
                return;
            case ClusterConstants.PARAM_TYPE_STRING:
                ensureReadable(source, Integer.BYTES);
                int length = source.readInt();
                if (length < 0 || length > ServerConstants.MAX_PARAM_STRING_LENGTH
                    || length > source.readableBytes()) {
                    throw new CorruptedFrameException("Invalid string parameter length: " + length);
                }
                byte[] bytes = new byte[length];
                source.readBytes(bytes);
                // TODO: take care of charset?
                params.add(new String(bytes));
                return;
            case ClusterConstants.PARAM_TYPE_BOOLEAN:
                ensureReadable(source, Byte.BYTES);
                params.add(source.readBoolean());
                return;
            case ClusterConstants.PARAM_TYPE_DOUBLE:
                ensureReadable(source, Double.BYTES);
                params.add(source.readDouble());
                return;
            case ClusterConstants.PARAM_TYPE_LONG:
                ensureReadable(source, Long.BYTES);
                params.add(source.readLong());
                return;
            case ClusterConstants.PARAM_TYPE_FLOAT:
                ensureReadable(source, Float.BYTES);
                params.add(source.readFloat());
                return;
            case ClusterConstants.PARAM_TYPE_BYTE:
                ensureReadable(source, Byte.BYTES);
                params.add(source.readByte());
                return;
            case ClusterConstants.PARAM_TYPE_SHORT:
                ensureReadable(source, Short.BYTES);
                params.add(source.readShort());
                return;
            default:
                throw new CorruptedFrameException("Unknown parameter type: " + paramType);
        }
    }

    private void ensureReadable(ByteBuf source, int requiredBytes) {
        if (source.readableBytes() < requiredBytes) {
            throw new CorruptedFrameException("Incomplete parameter flow payload: required=" + requiredBytes
                + ", actual=" + source.readableBytes());
        }
    }
}
