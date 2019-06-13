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
package com.alibaba.csp.sentinel.cluster.client.codec.data;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;
import com.alibaba.csp.sentinel.cluster.request.data.ParamFlowRequestData;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ParamFlowRequestDataWriter implements EntityWriter<ParamFlowRequestData, ByteBuf> {

    private final int maxParamByteSize;

    public ParamFlowRequestDataWriter() {
        this(DEFAULT_PARAM_MAX_SIZE);
    }

    public ParamFlowRequestDataWriter(int maxParamByteSize) {
        AssertUtil.isTrue(maxParamByteSize > 0, "maxParamByteSize should be positive");
        this.maxParamByteSize = maxParamByteSize;
    }

    @Override
    public void writeTo(ParamFlowRequestData entity, ByteBuf target) {
        target.writeLong(entity.getFlowId());
        target.writeInt(entity.getCount());

        Collection<Object> params = entity.getParams();

        params = resolveValidParams(params);
        target.writeInt(params.size());

        // Serialize parameters with type flag.
        for (Object param : params) {
            encodeValue(param, target);
        }
    }

    /**
     * Get valid parameters in provided parameter list
     *
     * @param params
     * @return
     */
    public List<Object> resolveValidParams(Collection<Object> params) {
        List<Object> validParams = new ArrayList<>();
        int size = 0;
        for (Object param : params) {
            int s = calculateParamTransportSize(param);
            if (s <= 0) {
                RecordLog.warn("[ParamFlowRequestDataWriter] WARN: Non-primitive type detected in params of "
                        + "cluster parameter flow control, which is not supported: " + param);
                continue;
            }
            if (size + s > maxParamByteSize) {
                RecordLog.warn("[ParamFlowRequestDataWriter] WARN: params size is too big." +
                        " the configure value is : " + maxParamByteSize + ", the params size is: " + params.size());
                break;
            }
            size += s;
            validParams.add(param);
        }
        return validParams;
    }

    private void encodeValue(Object param, ByteBuf target) {
        // Handle primitive type.
        if (param instanceof Integer || int.class.isInstance(param)) {
            target.writeByte(ClusterConstants.PARAM_TYPE_INTEGER);
            target.writeInt((Integer) param);
        } else if (param instanceof String) {
            encodeString((String) param, target);
        } else if (boolean.class.isInstance(param) || param instanceof Boolean) {
            target.writeByte(ClusterConstants.PARAM_TYPE_BOOLEAN);
            target.writeBoolean((Boolean) param);
        } else if (long.class.isInstance(param) || param instanceof Long) {
            target.writeByte(ClusterConstants.PARAM_TYPE_LONG);
            target.writeLong((Long) param);
        } else if (double.class.isInstance(param) || param instanceof Double) {
            target.writeByte(ClusterConstants.PARAM_TYPE_DOUBLE);
            target.writeDouble((Double) param);
        } else if (float.class.isInstance(param) || param instanceof Float) {
            target.writeByte(ClusterConstants.PARAM_TYPE_FLOAT);
            target.writeFloat((Float) param);
        } else if (byte.class.isInstance(param) || param instanceof Byte) {
            target.writeByte(ClusterConstants.PARAM_TYPE_BYTE);
            target.writeByte((Byte) param);
        } else if (short.class.isInstance(param) || param instanceof Short) {
            target.writeByte(ClusterConstants.PARAM_TYPE_SHORT);
            target.writeShort((Short) param);
        } else {
            // Unexpected type, drop.
        }
    }

    private void encodeString(String param, ByteBuf target) {
        target.writeByte(ClusterConstants.PARAM_TYPE_STRING);
        byte[] tmpChars = param.getBytes();
        target.writeInt(tmpChars.length);
        target.writeBytes(tmpChars);
    }


    int calculateParamTransportSize(Object value) {
        if (value == null) {
            return 0;
        }
        // Layout for primitives: |type flag(1)|value|
        // size = original size + type flag (1)
        if (value instanceof Integer || int.class.isInstance(value)) {
            return 5;
        } else if (value instanceof String) {
            // Layout for string: |type flag(1)|length(4)|string content|
            String tmpValue = (String) value;
            byte[] tmpChars = tmpValue.getBytes();
            return 1 + 4 + tmpChars.length;
        } else if (boolean.class.isInstance(value) || value instanceof Boolean) {
            return 2;
        } else if (long.class.isInstance(value) || value instanceof Long) {
            return 9;
        } else if (double.class.isInstance(value) || value instanceof Double) {
            return 9;
        } else if (float.class.isInstance(value) || value instanceof Float) {
            return 5;
        } else if (byte.class.isInstance(value) || value instanceof Byte) {
            return 2;
        } else if (short.class.isInstance(value) || value instanceof Short) {
            return 3;
        } else {
            // Ignore unexpected type.
            return 0;
        }
    }

    private static final int DEFAULT_PARAM_MAX_SIZE = 1024;
}
