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
import java.util.Set;

import com.alibaba.csp.sentinel.cluster.client.codec.data.BatchFlowRequestDataWriter;
import com.alibaba.csp.sentinel.cluster.request.data.BatchFlowRequestData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class BatchFlowRequestDataDecoderIntegrationTest {

    @Test
    public void testDecodeBatchFlowRequestData() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        ByteBuf buf = Unpooled.buffer();
        BatchFlowRequestDataWriter writer = new BatchFlowRequestDataWriter();
        BatchFlowRequestDataDecoder decoder = new BatchFlowRequestDataDecoder();

        Set<Long> ids = new HashSet<>();
        ids.add(12L);
        ids.add(23L);
        BatchFlowRequestData requestData = new BatchFlowRequestData()
            .setFlowIds(ids)
            .setCount(10)
            .setPriority(true);

        writer.writeTo(requestData, buf);
        embeddedChannel.writeInbound(buf);

        BatchFlowRequestData decoded = decoder.decode((ByteBuf)embeddedChannel.readInbound());
        assertThat(decoded.getFlowIds()).isEqualTo(ids);
        assertThat(decoded.getCount()).isEqualTo(10);
        assertThat(decoded.isPriority()).isEqualTo(true);

        buf.release();
        embeddedChannel.close();
    }
}
