/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.impl.bootstrap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.config.SocketConfig;
import org.apache.http.protocol.HttpService;

/**
 * @since 4.4
 */
class RequestListener implements Runnable {

    private final SocketConfig socketConfig;
    private final ServerSocket serversocket;
    private final HttpService httpService;
    private final HttpConnectionFactory<? extends HttpServerConnection> connectionFactory;
    private final ExceptionLogger exceptionLogger;
    private final ExecutorService executorService;
    private final AtomicBoolean terminated;

    public RequestListener(
            final SocketConfig socketConfig,
            final ServerSocket serversocket,
            final HttpService httpService,
            final HttpConnectionFactory<? extends HttpServerConnection> connectionFactory,
            final ExceptionLogger exceptionLogger,
            final ExecutorService executorService) {
        this.socketConfig = socketConfig;
        this.serversocket = serversocket;
        this.connectionFactory = connectionFactory;
        this.httpService = httpService;
        this.exceptionLogger = exceptionLogger;
        this.executorService = executorService;
        this.terminated = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        try {
            while (!isTerminated() && !Thread.interrupted()) {
                final Socket socket = this.serversocket.accept();
                socket.setSoTimeout(this.socketConfig.getSoTimeout());
                socket.setKeepAlive(this.socketConfig.isSoKeepAlive());
                socket.setTcpNoDelay(this.socketConfig.isTcpNoDelay());
                if (this.socketConfig.getRcvBufSize() > 0) {
                    socket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
                }
                if (this.socketConfig.getSndBufSize() > 0) {
                    socket.setSendBufferSize(this.socketConfig.getSndBufSize());
                }
                if (this.socketConfig.getSoLinger() >= 0) {
                    socket.setSoLinger(true, this.socketConfig.getSoLinger());
                }
                final HttpServerConnection conn = this.connectionFactory.createConnection(socket);
                final Worker worker = new Worker(this.httpService, conn, this.exceptionLogger);
                this.executorService.execute(worker);
            }
        } catch (final Exception ex) {
            this.exceptionLogger.log(ex);
        }
    }

    public boolean isTerminated() {
        return this.terminated.get();
    }

    public void terminate() throws IOException {
        if (this.terminated.compareAndSet(false, true)) {
            this.serversocket.close();
        }
    }

}
