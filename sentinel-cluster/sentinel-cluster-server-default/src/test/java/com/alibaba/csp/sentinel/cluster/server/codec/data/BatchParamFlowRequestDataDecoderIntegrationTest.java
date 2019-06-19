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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.cluster.client.codec.data.BatchParamFlowRequestDataWriter;
import com.alibaba.csp.sentinel.cluster.request.data.BatchParamFlowRequestData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class BatchParamFlowRequestDataDecoderIntegrationTest {

    @Test
    public void testDecodeBatchParamFlowRequestData() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        ByteBuf buf = Unpooled.buffer();
        BatchParamFlowRequestDataWriter writer = new BatchParamFlowRequestDataWriter();
        BatchParamFlowRequestDataDecoder decoder = new BatchParamFlowRequestDataDecoder();

        Set<Long> ids = new HashSet<>();
        ids.add(144L);
        ids.add(27712L);
        Map<Integer, Object> params = new HashMap<>();
        params.put(0, 1L);
        params.put(1, 3.1f);
        params.put(2, "foo");
        params.put(3, new Object());
        params.put(4, false);
        BatchParamFlowRequestData requestData = new BatchParamFlowRequestData()
            .setFlowIds(ids)
            .setCount(10)
            .setParamMap(params);

        writer.writeTo(requestData, buf);
        embeddedChannel.writeInbound(buf);

        BatchParamFlowRequestData decoded = decoder.decode((ByteBuf) embeddedChannel.readInbound());
        assertThat(decoded.getFlowIds()).isEqualTo(ids);
        assertThat(decoded.getCount()).isEqualTo(10);
        assertThat(decoded.getParamMap().size()).isEqualTo(params.size() - 1);
        assertThat(decoded.getParamMap().values()).isSubsetOf(params.values());
        assertThat(decoded.getParamMap()).doesNotContainKey(3);

        buf.release();
        embeddedChannel.close();
    }
}
