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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.request.data.ParamFlowRequestData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class ParamFlowRequestDataWriterTest {

    @Test
    public void testEncode() {
        ParamFlowRequestDataWriter writer = new ParamFlowRequestDataWriter();
        ByteBuf buf = Unpooled.buffer();
        long flowId = 1996;
        int count = 10;
        List<Object> params = new ArrayList<Object>() {{
            add(1);
            add(3L);
            add("Sentinel");
            add(new Object());
            add(3.14d);
        }};
        ParamFlowRequestData data = new ParamFlowRequestData()
            .setFlowId(flowId)
            .setCount(count)
            .setParams(params);
        writer.writeTo(data, buf);

        assertThat(buf.readLong()).isEqualTo(flowId);
        assertThat(buf.readInt()).isEqualTo(count);
        assertThat(buf.readInt()).isEqualTo(params.size() - 1);

        assertThat(buf.readByte()).isEqualTo((byte)ClusterConstants.PARAM_TYPE_INTEGER);
        assertThat(buf.readInt()).isEqualTo(1);
        assertThat(buf.readByte()).isEqualTo((byte)ClusterConstants.PARAM_TYPE_LONG);
        assertThat(buf.readLong()).isEqualTo(3L);
        assertThat(buf.readByte()).isEqualTo((byte)ClusterConstants.PARAM_TYPE_STRING);
        int strLen = buf.readInt();
        assertThat(strLen).isEqualTo("Sentinel".getBytes().length);
        byte[] bytes = new byte[strLen];
        buf.readBytes(bytes);
        assertThat(new String(bytes)).isEqualTo("Sentinel");
        assertThat(buf.readByte()).isEqualTo((byte)ClusterConstants.PARAM_TYPE_DOUBLE);
        assertThat(buf.readDouble()).isEqualTo(3.14d);

        buf.release();
    }
}
