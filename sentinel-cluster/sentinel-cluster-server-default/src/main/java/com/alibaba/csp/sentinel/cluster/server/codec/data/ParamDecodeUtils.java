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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;

import io.netty.buffer.ByteBuf;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
final class ParamDecodeUtils {

    static Map<Integer, Object> decodeParamMap(ByteBuf source) {
        int amount = source.readInt();
        Map<Integer, Object> paramMap = new HashMap<>();
        if (amount > 0) {
            for (int i = 0; i < amount; i++) {
                int paramIdx = source.readInt();
                Object param = decodeSingleParam(source);
                if (param != null) {
                    paramMap.put(paramIdx, param);
                }
            }
        }
        return paramMap;
    }

    static List<Object> decodeParams(ByteBuf source) {
        int amount = source.readInt();
        List<Object> params = new ArrayList<>();
        if (amount > 0) {
            for (int i = 0; i < amount; i++) {
                Object param = decodeSingleParam(source);
                if (param != null) {
                    params.add(param);
                }
            }
        }
        return params;
    }

    private static Object decodeSingleParam(ByteBuf source) {
        byte paramType = source.readByte();

        switch (paramType) {
            case ClusterConstants.PARAM_TYPE_INTEGER:
                return source.readInt();
            case ClusterConstants.PARAM_TYPE_STRING:
                int length = source.readInt();
                byte[] bytes = new byte[length];
                source.readBytes(bytes);
                // TODO: take care of charset?
                return new String(bytes);
            case ClusterConstants.PARAM_TYPE_BOOLEAN:
                return source.readBoolean();
            case ClusterConstants.PARAM_TYPE_DOUBLE:
                return source.readDouble();
            case ClusterConstants.PARAM_TYPE_LONG:
                return source.readLong();
            case ClusterConstants.PARAM_TYPE_FLOAT:
                return source.readFloat();
            case ClusterConstants.PARAM_TYPE_BYTE:
                return source.readByte();
            case ClusterConstants.PARAM_TYPE_SHORT:
                return source.readShort();
            default:
                return null;
        }
    }

    private ParamDecodeUtils() {}
}
