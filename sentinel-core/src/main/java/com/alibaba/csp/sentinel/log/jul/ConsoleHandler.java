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
package com.alibaba.csp.sentinel.log.jul;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * This Handler publishes log records to console by using {@link java.util.logging.StreamHandler}.
 *
 * Print log of WARNING level or above to System.err,
 * and print log of INFO level or below to System.out.
 *
 * To use this handler, add the following VM argument:
 * <pre>
 * -Dcsp.sentinel.log.output.type=console
 * </pre>
 *
 * @author cdfive
 */
class ConsoleHandler extends Handler {
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1,
            5,
            1,
            TimeUnit.HOURS,
            new ArrayBlockingQueue<>(1024),
            new NamedThreadFactory("sentinel-console-log-executor", true),
            new LogRejectedExecutionHandler()
    );

    static {
        executor.allowCoreThreadTimeOut(true);
    }

    /**
     * A Handler which publishes log records to System.out.
     */
    private StreamHandler stdoutHandler;

    /**
     * A Handler which publishes log records to System.err.
     */
    private StreamHandler stderrHandler;

    private AtomicReference<Future<?>> lastFuture = new AtomicReference<>();

    public ConsoleHandler() {
        this.stdoutHandler = new StreamHandler(System.out, new CspFormatter());
        this.stderrHandler = new StreamHandler(System.err, new CspFormatter());
    }

    @Override
    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        this.stdoutHandler.setFormatter(newFormatter);
        this.stderrHandler.setFormatter(newFormatter);
    }

    @Override
    public synchronized void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
        this.stdoutHandler.setEncoding(encoding);
        this.stderrHandler.setEncoding(encoding);
    }

    @Override
    public void publish(LogRecord record) {
        lastFuture.set(executor.submit(new LogTask(record, stdoutHandler, stderrHandler)));
    }

    @Override
    public void flush() {
        stdoutHandler.flush();
        stderrHandler.flush();
    }

    @Override
    public void close() throws SecurityException {
        Future<?> future = lastFuture.get();
        if (future != null) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        stdoutHandler.close();
        stderrHandler.close();
    }

    static class LogRejectedExecutionHandler implements RejectedExecutionHandler {

        /**
         * The period of logged rejected records.
         */
        private final long recordPeriod;

        private Long lastRecordTime;

        public LogRejectedExecutionHandler() {
            String DEFAULT_REJECTED_RECORD_PERIOD = "60000";
            String REJECTED_RECORD_PERIOD_KEY = "sentinel.rejected.record.period";
            lastRecordTime = null;
            recordPeriod = Long.parseLong(System.getProperty(REJECTED_RECORD_PERIOD_KEY, DEFAULT_REJECTED_RECORD_PERIOD));
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            long currentTimestamp = System.currentTimeMillis();
            if (lastRecordTime == null || currentTimestamp - lastRecordTime > recordPeriod) {
                System.err.println("Failed to log sentinel record with console, rejected");
                lastRecordTime = currentTimestamp;
            }
        }

    }

    static class LogTask implements Runnable {
        private final LogRecord record;
        private final StreamHandler stdoutHandler;
        private final StreamHandler stderrHandler;

        public LogTask(LogRecord record,StreamHandler stdoutHandler,StreamHandler stderrHandler) {
            this.record = record;
            this.stdoutHandler = stdoutHandler;
            this.stderrHandler = stderrHandler;
        }

        public void run() {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                stderrHandler.publish(record);
                stderrHandler.flush();
            } else {
                stdoutHandler.publish(record);
                stdoutHandler.flush();
            }
        }

        public LogRecord getRecord() {
            return record;
        }

    }

}
