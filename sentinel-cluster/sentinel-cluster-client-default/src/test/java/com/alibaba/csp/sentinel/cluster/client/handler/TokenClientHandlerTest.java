package com.alibaba.csp.sentinel.cluster.client.handler;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenClientHandlerTest {

    private Channel mockChannel;
    private ChannelHandlerContext mockCtx;
    private TokenClientHandler handler;

    @Before
    public void setUp() {
        mockChannel = mock(Channel.class);
        mockCtx = mock(ChannelHandlerContext.class);
        handler = new TokenClientHandler(new AtomicInteger(0), () -> { });
        when(mockCtx.channel()).thenReturn(mockChannel);
    }

    @Test
    public void testGetRemoteAddressWithUnresolvedSocketAddress() throws Exception {
        InetSocketAddress unresolved = InetSocketAddress.createUnresolved("unresolved.host", 8710);
        Assert.assertNull("Unresolved address should have null InetAddress", unresolved.getAddress());
        when(mockChannel.remoteAddress()).thenReturn(unresolved);
        Method method = TokenClientHandler.class.getDeclaredMethod("getRemoteAddress", ChannelHandlerContext.class);
        method.setAccessible(true);
        try {
            String result = (String) method.invoke(handler, mockCtx);
            Assert.assertNotNull("Should return a non-null result for unresolved address", result);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof NullPointerException) {
                Assert.fail("getRemoteAddress should handle unresolved InetSocketAddress without NPE, but got: " + e.getCause());
            }
            throw e;
        }
    }
}
