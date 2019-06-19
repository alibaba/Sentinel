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

import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.cluster.request.data.BatchFlowRequestData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class BatchFlowRequestDataWriterTest {

    @Test
    public void testEncodeBatchFlowRequestData() {
        ByteBuf buf = Unpooled.buffer();
        BatchFlowRequestDataWriter writer = new BatchFlowRequestDataWriter();

        // Test for empty ID set.
        writer.writeTo(new BatchFlowRequestData().setCount(1).setPriority(false), buf);
        assertThat(buf.readableBytes()).isZero();

        Set<Long> ids = new HashSet<>();
        ids.add(13L);
        ids.add(177L);
        BatchFlowRequestData data = new BatchFlowRequestData()
            .setCount(19)
            .setPriority(true)
            .setFlowIds(ids);
        writer.writeTo(data, buf);

        assertThat(buf.readInt()).isEqualTo(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            assertThat(ids.contains(buf.readLong())).isTrue();
        }
        assertThat(buf.readInt()).isEqualTo(19);
        assertThat(buf.readBoolean()).isEqualTo(true);

        buf.release();
    }
}
