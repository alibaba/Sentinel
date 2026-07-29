/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test cases for {@link PingRequestDataDecoder}.
 */
public class PingRequestDataDecoderTest {

    private final PingRequestDataDecoder decoder = new PingRequestDataDecoder();

    @Test
    public void testDecodeValidNamespace() {
        ByteBuf source = pingPayload("default");
        try {
            assertThat(decoder.decode(source)).isEqualTo("default");
            assertThat(source.isReadable()).isFalse();
        } finally {
            source.release();
        }
    }

    @Test
    public void testDecodeEmptyNamespace() {
        ByteBuf source = Unpooled.buffer().writeInt(0);
        try {
            assertThat(decoder.decode(source)).isEmpty();
            assertThat(source.isReadable()).isFalse();
        } finally {
            source.release();
        }
    }

    @Test
    public void testRejectNmapProbeBeforeAllocation() {
        ByteBuf source = Unpooled.buffer()
            .writeInt(0x01000000)
            .writeZero(21);
        try {
            assertThatThrownBy(() -> decoder.decode(source))
                .isInstanceOf(CorruptedFrameException.class);
        } finally {
            source.release();
        }
    }

    @Test
    public void testRejectNegativeLength() {
        assertMalformed(Unpooled.buffer().writeInt(-1));
    }

    @Test
    public void testRejectDeclaredLengthGreaterThanRemainingBytes() {
        assertMalformed(Unpooled.buffer().writeInt(4).writeZero(3));
    }

    @Test
    public void testRejectDeclaredLengthLessThanRemainingBytes() {
        assertMalformed(Unpooled.buffer().writeInt(2).writeZero(3));
    }

    @Test
    public void testRejectTruncatedLengthField() {
        assertMalformed(Unpooled.buffer().writeZero(3));
    }

    private void assertMalformed(ByteBuf source) {
        try {
            assertThatThrownBy(() -> decoder.decode(source))
                .isInstanceOf(CorruptedFrameException.class);
        } finally {
            source.release();
        }
    }

    private ByteBuf pingPayload(String namespace) {
        byte[] bytes = namespace.getBytes(StandardCharsets.UTF_8);
        return Unpooled.buffer()
            .writeInt(bytes.length)
            .writeBytes(bytes);
    }
}
