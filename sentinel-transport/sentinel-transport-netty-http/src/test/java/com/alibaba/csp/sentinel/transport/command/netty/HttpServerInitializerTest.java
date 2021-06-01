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
package com.alibaba.csp.sentinel.transport.command.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

/**
 * Test cases for {@link HttpServerInitializer}.
 *
 * @author cdfive
 */
public class HttpServerInitializerTest {

    @Test
    public void testInitChannel() throws Exception {
        // Mock Objects
        HttpServerInitializer httpServerInitializer = mock(HttpServerInitializer.class);
        SocketChannel socketChannel = mock(SocketChannel.class);
        ChannelPipeline channelPipeline = mock(ChannelPipeline.class);

        // Mock SocketChannel#pipeline() method
        when(socketChannel.pipeline()).thenReturn(channelPipeline);

        // HttpServerInitializer#initChannel(SocketChannel) call real method
        doCallRealMethod().when(httpServerInitializer).initChannel(socketChannel);

        // Start test for HttpServerInitializer#initChannel(SocketChannel)
        httpServerInitializer.initChannel(socketChannel);

        // Verify 4 times calling ChannelPipeline#addLast() method
        verify(channelPipeline, times(4)).addLast(any(ChannelHandler.class));

        // Verify the order of calling ChannelPipeline#addLast() method
        InOrder inOrder = inOrder(channelPipeline);
        inOrder.verify(channelPipeline).addLast(any(HttpRequestDecoder.class));
        inOrder.verify(channelPipeline).addLast(any(HttpObjectAggregator.class));
        inOrder.verify(channelPipeline).addLast(any(HttpResponseEncoder.class));

        inOrder.verify(channelPipeline).addLast(any(HttpServerHandler.class));
    }
}
