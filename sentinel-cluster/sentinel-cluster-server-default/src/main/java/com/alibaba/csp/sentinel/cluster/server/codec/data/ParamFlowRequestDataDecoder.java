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
import java.util.List;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;
import com.alibaba.csp.sentinel.cluster.request.data.ParamFlowRequestData;

import io.netty.buffer.ByteBuf;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ParamFlowRequestDataDecoder implements EntityDecoder<ByteBuf, ParamFlowRequestData> {

    @Override
    public ParamFlowRequestData decode(ByteBuf source) {
        if (source.readableBytes() >= 16) {
            ParamFlowRequestData requestData = new ParamFlowRequestData()
                .setFlowId(source.readLong())
                .setCount(source.readInt());

            int amount = source.readInt();
            if (amount > 0) {
                List<Object> params = new ArrayList<>(amount);
                for (int i = 0; i < amount; i++) {
                    decodeParam(source, params);
                }

                requestData.setParams(params);
                return requestData;
            }
        }
        return null;
    }

    private boolean decodeParam(ByteBuf source, List<Object> params) {
        byte paramType = source.readByte();

        switch (paramType) {
            case ClusterConstants.PARAM_TYPE_INTEGER:
                params.add(source.readInt());
                return true;
            case ClusterConstants.PARAM_TYPE_STRING:
                int length = source.readInt();
                byte[] bytes = new byte[length];
                source.readBytes(bytes);
                // TODO: take care of charset?
                params.add(new String(bytes));
                return true;
            case ClusterConstants.PARAM_TYPE_BOOLEAN:
                params.add(source.readBoolean());
                return true;
            case ClusterConstants.PARAM_TYPE_DOUBLE:
                params.add(source.readDouble());
                return true;
            case ClusterConstants.PARAM_TYPE_LONG:
                params.add(source.readLong());
                return true;
            case ClusterConstants.PARAM_TYPE_FLOAT:
                params.add(source.readFloat());
                return true;
            case ClusterConstants.PARAM_TYPE_BYTE:
                params.add(source.readByte());
                return true;
            case ClusterConstants.PARAM_TYPE_SHORT:
                params.add(source.readShort());
                return true;
            default:
                return false;
        }
    }
}
