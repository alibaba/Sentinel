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
import java.util.Arrays;
import java.util.Collections;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.request.data.ParamFlowRequestData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test cases for {@link ParamFlowRequestDataDecoder}.
 */
public class ParamFlowRequestDataDecoderTest {

    private final ParamFlowRequestDataDecoder decoder = new ParamFlowRequestDataDecoder();

    @Test
    public void testDecodeAllSupportedParamTypes() {
        ByteBuf source = requestHeader(8)
            .writeByte(ClusterConstants.PARAM_TYPE_INTEGER).writeInt(1)
            .writeByte(ClusterConstants.PARAM_TYPE_STRING).writeInt(8)
            .writeBytes("Sentinel".getBytes(StandardCharsets.UTF_8))
            .writeByte(ClusterConstants.PARAM_TYPE_BOOLEAN).writeBoolean(true)
            .writeByte(ClusterConstants.PARAM_TYPE_DOUBLE).writeDouble(2.5D)
            .writeByte(ClusterConstants.PARAM_TYPE_LONG).writeLong(3L)
            .writeByte(ClusterConstants.PARAM_TYPE_FLOAT).writeFloat(4.5F)
            .writeByte(ClusterConstants.PARAM_TYPE_BYTE).writeByte(5)
            .writeByte(ClusterConstants.PARAM_TYPE_SHORT).writeShort(6);
        try {
            ParamFlowRequestData result = decoder.decode(source);

            assertThat(result.getFlowId()).isEqualTo(1L);
            assertThat(result.getCount()).isEqualTo(2);
            assertThat(result.getParams()).containsExactly(1, "Sentinel", true, 2.5D, 3L, 4.5F, (byte)5, (short)6);
            assertThat(source.isReadable()).isFalse();
        } finally {
            source.release();
        }
    }

    @Test
    public void testDecodeEmptyParams() {
        ByteBuf source = requestHeader(0);
        try {
            ParamFlowRequestData result = decoder.decode(source);

            assertThat(result.getParams()).isEqualTo(Collections.emptyList());
            assertThat(source.isReadable()).isFalse();
        } finally {
            source.release();
        }
    }

    @Test
    public void testRejectNegativeAmount() {
        assertMalformed(requestHeader(-1));
    }

    @Test
    public void testRejectAmountAboveLimit() {
        ByteBuf source = requestHeader(513);
        for (int i = 0; i < 513; i++) {
            source.writeByte(ClusterConstants.PARAM_TYPE_BYTE).writeByte(1);
        }
        assertMalformed(source);
    }

    @Test
    public void testRejectAmountThatCannotFitRemainingBytes() {
        assertMalformed(requestHeader(2)
            .writeByte(ClusterConstants.PARAM_TYPE_BYTE)
            .writeByte(1));
    }

    @Test
    public void testRejectMissingParamType() {
        assertMalformed(requestHeader(1));
    }

    @Test
    public void testRejectTruncatedPrimitiveValues() {
        for (TruncatedParam param : Arrays.asList(
            new TruncatedParam((byte)ClusterConstants.PARAM_TYPE_INTEGER, 3),
            new TruncatedParam((byte)ClusterConstants.PARAM_TYPE_BOOLEAN, 0),
            new TruncatedParam((byte)ClusterConstants.PARAM_TYPE_DOUBLE, 7),
            new TruncatedParam((byte)ClusterConstants.PARAM_TYPE_LONG, 7),
            new TruncatedParam((byte)ClusterConstants.PARAM_TYPE_FLOAT, 3),
            new TruncatedParam((byte)ClusterConstants.PARAM_TYPE_BYTE, 0),
            new TruncatedParam((byte)ClusterConstants.PARAM_TYPE_SHORT, 1))) {
            assertMalformed(requestHeader(1)
                .writeByte(param.type)
                .writeZero(param.valueBytes));
        }
    }

    @Test
    public void testRejectNegativeStringLength() {
        assertMalformed(requestHeader(1)
            .writeByte(ClusterConstants.PARAM_TYPE_STRING)
            .writeInt(-1));
    }

    @Test
    public void testRejectStringLengthAboveLimit() {
        assertMalformed(requestHeader(1)
            .writeByte(ClusterConstants.PARAM_TYPE_STRING)
            .writeInt(1025)
            .writeZero(1025));
    }

    @Test
    public void testRejectStringLengthGreaterThanRemainingBytes() {
        assertMalformed(requestHeader(1)
            .writeByte(ClusterConstants.PARAM_TYPE_STRING)
            .writeInt(4)
            .writeZero(3));
    }

    @Test
    public void testRejectUnknownParamType() {
        assertMalformed(requestHeader(1)
            .writeByte(Byte.MAX_VALUE)
            .writeByte(1));
    }

    @Test
    public void testRejectTrailingBytesWithEmptyParams() {
        assertMalformed(requestHeader(0).writeByte(1));
    }

    @Test
    public void testRejectTrailingBytesAfterDeclaredParams() {
        assertMalformed(requestHeader(1)
            .writeByte(ClusterConstants.PARAM_TYPE_BYTE)
            .writeByte(1)
            .writeByte(2));
    }

    @Test
    public void testRejectTruncatedRequestHeader() {
        assertMalformed(Unpooled.buffer().writeZero(15));
    }

    private ByteBuf requestHeader(int amount) {
        return Unpooled.buffer()
            .writeLong(1L)
            .writeInt(2)
            .writeInt(amount);
    }

    private void assertMalformed(ByteBuf source) {
        try {
            assertThatThrownBy(() -> decoder.decode(source))
                .isInstanceOf(CorruptedFrameException.class);
        } finally {
            source.release();
        }
    }

    private static final class TruncatedParam {
        private final byte type;
        private final int valueBytes;

        private TruncatedParam(byte type, int valueBytes) {
            this.type = type;
            this.valueBytes = valueBytes;
        }
    }
}
