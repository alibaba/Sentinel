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
package com.alibaba.csp.sentinel.cluster.server.connection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.netty.channel.Channel;

/**
 * @author xuyue
 * @since 1.4.0
 */
public class NettyConnection implements Connection {

    private String remoteIp;
    private int remotePort;
    private Channel channel;

    private long lastReadTime;

    private ConnectionPool pool;

    public NettyConnection(Channel channel, ConnectionPool pool) {
        this.channel = channel;
        this.pool = pool;

        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        this.remoteIp = socketAddress.getAddress().getHostAddress();
        this.remotePort = socketAddress.getPort();
        this.lastReadTime = System.currentTimeMillis();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return channel.localAddress();
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getRemoteIP() {
        return remoteIp;
    }

    @Override
    public void refreshLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    @Override
    public long getLastReadTime() {
        return lastReadTime;
    }

    @Override
    public String getConnectionKey() {
        return remoteIp + ":" + remotePort;
    }

    @Override
    public void close() {
        // Remove from connection pool.
        pool.remove(channel);
        // Close the connection.
        if (channel != null && channel.isActive()){
            channel.close();
        }
    }
}