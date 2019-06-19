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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 */
public class PingResponseDataDecoderTest {

    @Test
    public void testDecodePingResponseData() {
        ByteBuf buf = Unpooled.buffer();
        PingResponseDataDecoder decoder = new PingResponseDataDecoder();

        int big = Integer.MAX_VALUE;
        buf.writeInt(big);
        assertThat(decoder.decode(buf)).isEqualTo(big);

        byte small = 12;
        buf.writeByte(small);
        assertThat(decoder.decode(buf)).isEqualTo(small);

        buf.release();
    }
}
