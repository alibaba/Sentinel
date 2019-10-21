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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.HttpService;

/**
 * @since 4.4
 */
public class HttpServer {

    enum Status { READY, ACTIVE, STOPPING }

    private final int port;
    private final InetAddress ifAddress;
    private final SocketConfig socketConfig;
    private final ServerSocketFactory serverSocketFactory;
    private final HttpService httpService;
    private final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;
    private final SSLServerSetupHandler sslSetupHandler;
    private final ExceptionLogger exceptionLogger;
    private final ThreadPoolExecutor listenerExecutorService;
    private final ThreadGroup workerThreads;
    private final WorkerPoolExecutor workerExecutorService;
    private final AtomicReference<Status> status;

    private volatile ServerSocket serverSocket;
    private volatile RequestListener requestListener;

    HttpServer(
            final int port,
            final InetAddress ifAddress,
            final SocketConfig socketConfig,
            final ServerSocketFactory serverSocketFactory,
            final HttpService httpService,
            final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory,
            final SSLServerSetupHandler sslSetupHandler,
            final ExceptionLogger exceptionLogger) {
        this.port = port;
        this.ifAddress = ifAddress;
        this.socketConfig = socketConfig;
        this.serverSocketFactory = serverSocketFactory;
        this.httpService = httpService;
        this.connectionFactory = connectionFactory;
        this.sslSetupHandler = sslSetupHandler;
        this.exceptionLogger = exceptionLogger;
        this.listenerExecutorService = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactoryImpl("HTTP-listener-" + this.port));
        this.workerThreads = new ThreadGroup("HTTP-workers");
        this.workerExecutorService = new WorkerPoolExecutor(
                0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactoryImpl("HTTP-worker", this.workerThreads));
        this.status = new AtomicReference<Status>(Status.READY);
    }

    public InetAddress getInetAddress() {
        final ServerSocket localSocket = this.serverSocket;
        if (localSocket != null) {
            return localSocket.getInetAddress();
        } else {
            return null;
        }
    }

    public int getLocalPort() {
        final ServerSocket localSocket = this.serverSocket;
        if (localSocket != null) {
            return localSocket.getLocalPort();
        } else {
            return -1;
        }
    }

    public void start() throws IOException {
        if (this.status.compareAndSet(Status.READY, Status.ACTIVE)) {
            this.serverSocket = this.serverSocketFactory.createServerSocket(
                    this.port, this.socketConfig.getBacklogSize(), this.ifAddress);
            this.serverSocket.setReuseAddress(this.socketConfig.isSoReuseAddress());
            if (this.socketConfig.getRcvBufSize() > 0) {
                this.serverSocket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
            }
            if (this.sslSetupHandler != null && this.serverSocket instanceof SSLServerSocket) {
                this.sslSetupHandler.initialize((SSLServerSocket) this.serverSocket);
            }
            this.requestListener = new RequestListener(
                    this.socketConfig,
                    this.serverSocket,
                    this.httpService,
                    this.connectionFactory,
                    this.exceptionLogger,
                    this.workerExecutorService);
            this.listenerExecutorService.execute(this.requestListener);
        }
    }

    public void stop() {
        if (this.status.compareAndSet(Status.ACTIVE, Status.STOPPING)) {
            this.listenerExecutorService.shutdown();
            this.workerExecutorService.shutdown();
            final RequestListener local = this.requestListener;
            if (local != null) {
                try {
                    local.terminate();
                } catch (final IOException ex) {
                    this.exceptionLogger.log(ex);
                }
            }
            this.workerThreads.interrupt();
        }
    }

    public void awaitTermination(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        this.workerExecutorService.awaitTermination(timeout, timeUnit);
    }

    public void shutdown(final long gracePeriod, final TimeUnit timeUnit) {
        stop();
        if (gracePeriod > 0) {
            try {
                awaitTermination(gracePeriod, timeUnit);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        final Set<Worker> workers = this.workerExecutorService.getWorkers();
        for (final Worker worker: workers) {
            final HttpServerConnection conn = worker.getConnection();
            try {
                conn.shutdown();
            } catch (final IOException ex) {
                this.exceptionLogger.log(ex);
            }
        }
    }

}
