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
package com.alibaba.csp.sentinel.transport.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.command.netty.HttpServer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Regression tests for the lifecycle and thread behavior of {@link NettyHttpCommandCenter}.
 *
 * <p>Regression test for
 * <a href="https://github.com/alibaba/Sentinel/issues/2964">issue #2964</a>: the command
 * center executor and Netty boss/worker event-loop threads must be daemon threads, so
 * that the JVM can exit normally when the application (e.g. Spring Boot) is shut down
 * without an explicit {@code stop()} call on the command center.
 */
public class NettyHttpCommandCenterTest {

    private static final String BOSS_THREAD_PREFIX = "sentinel-netty-http-boss";
    private static final String WORKER_THREAD_PREFIX = "sentinel-netty-http-worker";
    private static final String EXECUTOR_THREAD_PREFIX = "sentinel-netty-command-center-executor";

    private static final long POLL_INTERVAL_MS = 500L;
    private static final long POLL_TIMEOUT_MS = 10000L;

    private static final String READY_MARKER = "COMMAND_CENTER_READY";
    private static final String STOPPED_MARKER = "COMMAND_CENTER_STOPPED";

    @Test
    public void testAllThreadsDaemon() throws Exception {
        TransportConfig.setRuntimePort(probeFreePort());
        NettyHttpCommandCenter commandCenter = new NettyHttpCommandCenter();
        try {
            commandCenter.beforeStart();
            commandCenter.start();

            int port = awaitListening();
            // Send a real request so that a worker event loop thread is created.
            assertCommandRequestWorks(port);
            awaitWorkerThreadCreated();

            assertAllCommandCenterThreadsDaemon();
        } finally {
            commandCenter.stop();
            TransportConfig.setRuntimePort(-1);
        }
    }

    @Test
    public void testJvmExitsWithoutExplicitStop() throws Exception {
        File outputFile = File.createTempFile("sentinel-exit-test-", ".log");
        try {
            Process process = startTestProcess(ExitTestMain.class, outputFile);
            awaitSuccessfulExit(process, outputFile, "without an explicit stop");
            assertTrue("Child JVM exited before the command center was ready:\n" + readFile(outputFile),
                readFile(outputFile).contains(READY_MARKER));
        } finally {
            outputFile.delete();
        }
    }

    @Test
    public void testExplicitStopDoesNotPrintInterruptedException() throws Exception {
        File outputFile = File.createTempFile("sentinel-stop-test-", ".log");
        try {
            Process process = startTestProcess(StopTestMain.class, outputFile);
            awaitSuccessfulExit(process, outputFile, "after an explicit stop");

            String output = readFile(outputFile);
            assertTrue("Child JVM did not complete the explicit stop:\n" + output,
                output.contains(STOPPED_MARKER));
            assertFalse("Normal stop must not be reported as an interruption:\n" + output,
                output.contains("InterruptedException"));
        } finally {
            outputFile.delete();
        }
    }

    @Test
    public void testCloseBeforeStartIsHonored() throws Exception {
        int port = probeFreePort();
        TransportConfig.setRuntimePort(port);
        HttpServer server = new HttpServer();
        AtomicReference<Throwable> failure = new AtomicReference<Throwable>();

        // Simulate the application context closing before the asynchronous server task begins.
        server.close();
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (Throwable t) {
                    failure.set(t);
                }
            }
        }, "sentinel-close-before-start-test");
        serverThread.setDaemon(true);

        try {
            serverThread.start();
            serverThread.join(POLL_TIMEOUT_MS);
            assertFalse("HttpServer did not honor a stop request issued before bind", serverThread.isAlive());
            assertNull("HttpServer failed while honoring an early stop", failure.get());
            assertFalse("HttpServer left its port open after an early stop", canConnect(port));
        } finally {
            TransportConfig.setRuntimePort(-1);
        }
    }

    /**
     * Entry point for the child JVM of {@link #testJvmExitsWithoutExplicitStop()}.
     *
     * <p>Starts the Netty command center and returns from {@code main()} without calling
     * {@code stop()}, reproducing the scenario of issue #2964: the JVM must exit on its own
     * since all command center threads are daemon threads.
     */
    public static class ExitTestMain {

        public static void main(String[] args) throws Exception {
            TransportConfig.setRuntimePort(probeFreePort());
            NettyHttpCommandCenter commandCenter = new NettyHttpCommandCenter();
            commandCenter.beforeStart();
            commandCenter.start();

            awaitListeningOrThrow();
            System.out.println(READY_MARKER);
            // Intentionally return without calling stop().
        }
    }

    /**
     * Entry point for verifying that an explicit stop finishes without printing
     * an interruption stack trace.
     */
    public static class StopTestMain {

        public static void main(String[] args) throws Exception {
            TransportConfig.setRuntimePort(probeFreePort());
            NettyHttpCommandCenter commandCenter = new NettyHttpCommandCenter();
            commandCenter.beforeStart();
            commandCenter.start();

            awaitListeningOrThrow();
            System.out.println(READY_MARKER);
            commandCenter.stop();
            System.out.println(STOPPED_MARKER);
        }
    }

    private static Process startTestProcess(Class<?> mainClass, File outputFile) throws IOException {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        ProcessBuilder processBuilder = new ProcessBuilder(javaBin, "-cp", System.getProperty("java.class.path"),
            mainClass.getName());
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(outputFile);
        return processBuilder.start();
    }

    private static void awaitSuccessfulExit(Process process, File outputFile, String scenario) throws Exception {
        boolean exited = process.waitFor(20, TimeUnit.SECONDS);
        if (!exited) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            fail("Child JVM did not exit within 20 seconds " + scenario
                + ": the Netty command center is keeping the JVM alive (issue #2964).\n"
                + readFile(outputFile));
        }
        int exitCode = process.exitValue();
        assertEquals("Child JVM should exit with code 0, but got " + exitCode + ":\n" + readFile(outputFile),
            0, exitCode);
    }

    private static int awaitListeningOrThrow() throws Exception {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            int port = TransportConfig.getRuntimePort();
            if (port > 0 && canConnect(port)) {
                return port;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        throw new IllegalStateException("Netty command center did not start listening within "
            + POLL_TIMEOUT_MS + " ms");
    }

    /**
     * Probe a free TCP port. There is a small TOCTOU window between this probe and the actual
     * bind, so callers should not assume the probed port is the one actually used; poll
     * {@link TransportConfig#getRuntimePort()} instead.
     */
    private static int probeFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    private static int awaitListening() throws Exception {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            int port = TransportConfig.getRuntimePort();
            if (port > 0 && canConnect(port)) {
                return port;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        fail("Netty command center did not start listening within " + POLL_TIMEOUT_MS + " ms");
        return -1;
    }

    private static boolean canConnect(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 200);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void assertCommandRequestWorks(int port) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 1000);
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            out.write("GET /version HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"
                .getBytes(StandardCharsets.UTF_8));
            out.flush();
            String response = readResponse(socket.getInputStream());
            assertTrue("Unexpected response from command center: " + response, response.startsWith("HTTP/1.1 200"));
        }
    }

    private static String readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void awaitWorkerThreadCreated() throws InterruptedException {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (hasThreadWithPrefix(WORKER_THREAD_PREFIX)) {
                return;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        fail("No worker event loop thread created (prefix: " + WORKER_THREAD_PREFIX + ")");
    }

    private static boolean hasThreadWithPrefix(String prefix) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Asserts that all command center threads (boss/worker event loops and the executor
     * thread) exist and are daemon threads. Checks the actually running threads instead of
     * the thread factory configuration, so that a regression would fail this test.
     */
    private static void assertAllCommandCenterThreadsDaemon() {
        boolean bossSeen = false;
        boolean workerSeen = false;
        boolean executorSeen = false;
        List<String> nonDaemonThreads = new ArrayList<String>();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            String name = thread.getName();
            boolean boss = name.startsWith(BOSS_THREAD_PREFIX);
            boolean worker = name.startsWith(WORKER_THREAD_PREFIX);
            boolean executor = name.startsWith(EXECUTOR_THREAD_PREFIX);
            if (boss) {
                bossSeen = true;
            }
            if (worker) {
                workerSeen = true;
            }
            if (executor) {
                executorSeen = true;
            }
            if (boss || worker || executor) {
                if (!thread.isDaemon()) {
                    nonDaemonThreads.add(name);
                }
            }
        }
        assertTrue("Boss event loop thread not found (prefix: " + BOSS_THREAD_PREFIX + ")", bossSeen);
        assertTrue("Worker event loop thread not found (prefix: " + WORKER_THREAD_PREFIX + ")", workerSeen);
        assertTrue("Command center executor thread not found (prefix: " + EXECUTOR_THREAD_PREFIX + ")", executorSeen);
        assertTrue("Non-daemon command center threads found: " + nonDaemonThreads, nonDaemonThreads.isEmpty());
    }

    private static String readFile(File file) throws IOException {
        if (file == null || !file.exists() || file.length() == 0) {
            return "";
        }
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
