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
package com.alibaba.csp.sentinel.cluster.server.handler;

import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionPool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test cases for {@link TokenServerHandler}.
 */
public class TokenServerHandlerTest {

    @Test
    public void testCloseConnectionOnCorruptedFrame() {
        assertExceptionClosesConnection(new CorruptedFrameException("malformed"));
    }

    @Test
    public void testCloseConnectionOnOversizedFrame() {
        assertExceptionClosesConnection(new TooLongFrameException("oversized"));
    }

    private void assertExceptionClosesConnection(Throwable cause) {
        EmbeddedChannel channel = new EmbeddedChannel(new IgnoreChannelInactiveHandler(),
            new TokenServerHandler(mock(ConnectionPool.class)));
        try {
            channel.pipeline().fireExceptionCaught(cause);
            channel.runPendingTasks();

            assertThat(channel.isActive()).isFalse();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static final class IgnoreChannelInactiveHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            // EmbeddedChannel has no remote address; stop before the production connection cleanup.
        }
    }
}
