/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for {@link PingResponseDataWriter}.
 *
 * @author Eric Zhao
 */
public class PingResponseDataWriterTest {

    @Test
    public void testWritePingResponseAndParse() {
        ByteBuf buf = Unpooled.buffer();
        PingResponseDataWriter writer = new PingResponseDataWriter();

        int small = 120;
        writer.writeTo(small, buf);
        assertThat(buf.readableBytes()).isGreaterThanOrEqualTo(4);
        assertThat(buf.readInt()).isEqualTo(small);

        int big = Integer.MAX_VALUE;
        writer.writeTo(big, buf);
        assertThat(buf.readableBytes()).isGreaterThanOrEqualTo(4);
        assertThat(buf.readInt()).isEqualTo(big);

        buf.release();
    }
}
