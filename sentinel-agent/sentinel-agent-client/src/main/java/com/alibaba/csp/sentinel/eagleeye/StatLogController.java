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
package com.alibaba.csp.sentinel.eagleeye;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class StatLogController {

    private static final Map<String, StatLogger> statLoggers = new ConcurrentHashMap<String, StatLogger>();

    private static final int STAT_ENTRY_COOL_DOWN_MILLIS = 200;

    private static final ScheduledThreadPoolExecutor rollerThreadPool =
        new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(
            "EagleEye-StatLogController-roller", true));

    private static final ScheduledThreadPoolExecutor writerThreadPool =
        new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(
            "EagleEye-StatLogController-writer", true));

    private static AtomicBoolean running = new AtomicBoolean(false);

    static StatLogger createLoggerIfNotExists(StatLoggerBuilder builder) {
        String loggerName = builder.getLoggerName();
        StatLogger statLogger = statLoggers.get(loggerName);
        if (statLogger == null) {
            synchronized (StatLogController.class) {
                if ((statLogger = statLoggers.get(loggerName)) == null) {
                    statLogger = builder.create();
                    statLoggers.put(loggerName, statLogger);

                    writerThreadPool.setMaximumPoolSize(Math.max(1, statLoggers.size()));

                    scheduleNextRollingTask(statLogger);
                    EagleEye.selfLog("[INFO] created statLogger[" + statLogger.getLoggerName() +
                        "]: " + statLogger.getAppender());
                }
            }
        }
        return statLogger;
    }

    static Map<String, StatLogger> getAllStatLoggers() {
        return Collections.unmodifiableMap(statLoggers);
    }

    private static void scheduleNextRollingTask(StatLogger statLogger) {
        if (!running.get()) {
            EagleEye.selfLog("[INFO] stopped rolling statLogger[" + statLogger.getLoggerName() + "]");
            return;
        }

        StatLogRollingTask rollingTask = new StatLogRollingTask(statLogger);

        long rollingTimeMillis = statLogger.getRollingData().getRollingTimeMillis();
        long delayMillis = rollingTimeMillis - System.currentTimeMillis();
        if (delayMillis > 5) {
            rollerThreadPool.schedule(rollingTask, delayMillis, TimeUnit.MILLISECONDS);
        } else if (-delayMillis > statLogger.getIntervalMillis()) {
            EagleEye.selfLog("[WARN] unusual delay of statLogger[" + statLogger.getLoggerName() +
                "], delay=" + (-delayMillis) + "ms, submit now");
            rollerThreadPool.submit(rollingTask);
        } else {
            rollerThreadPool.submit(rollingTask);
        }
    }

    static void scheduleWriteTask(StatRollingData statRollingData) {
        if (statRollingData != null) {
            try {
                StatLogWriteTask task = new StatLogWriteTask(statRollingData);
                writerThreadPool.schedule(task, STAT_ENTRY_COOL_DOWN_MILLIS, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                EagleEye.selfLog("[ERROR] fail to roll statLogger[" +
                    statRollingData.getStatLogger().getLoggerName() + "]", t);
            }
        }
    }

    private static class StatLogRollingTask implements Runnable {

        final StatLogger statLogger;

        StatLogRollingTask(StatLogger statLogger) {
            this.statLogger = statLogger;
        }

        @Override
        public void run() {
            scheduleWriteTask(statLogger.rolling());
            scheduleNextRollingTask(statLogger);
        }
    }

    private static class StatLogWriteTask implements Runnable {

        final StatRollingData statRollingData;

        StatLogWriteTask(StatRollingData statRollingData) {
            this.statRollingData = statRollingData;
        }

        @Override
        public void run() {
            final StatRollingData data = statRollingData;
            final StatLogger logger = data.getStatLogger();
            try {
                final FastDateFormat fmt = new FastDateFormat();
                final StringBuilder buffer = new StringBuilder(256);
                final String timeStr = fmt.formatWithoutMs(data.getTimeSlot());

                final EagleEyeAppender appender = logger.getAppender();
                final Set<Entry<StatEntry, StatEntryFunc>> entrySet = data.getStatEntrySet();
                final char entryDelimiter = logger.getEntryDelimiter();
                final char keyDelimiter = logger.getKeyDelimiter();
                final char valueDelimiter = logger.getValueDelimiter();

                for (Entry<StatEntry, StatEntryFunc> entry : entrySet) {
                    buffer.delete(0, buffer.length());
                    StatEntryFunc func = entry.getValue();
                    // time|statType|keys|values
                    buffer.append(timeStr).append(entryDelimiter);
                    buffer.append(func.getStatType()).append(entryDelimiter);
                    entry.getKey().appendTo(buffer, keyDelimiter);
                    buffer.append(entryDelimiter);
                    func.appendTo(buffer, valueDelimiter);
                    buffer.append(EagleEyeCoreUtils.NEWLINE);
                    appender.append(buffer.toString());
                }

                appender.flush();
            } catch (Throwable t) {
                EagleEye.selfLog("[WARN] fail to write statLogger[" +
                    logger.getLoggerName() + "]", t);
            }
        }
    }

    static void start() {
        if (running.compareAndSet(false, true)) {
            rollerThreadPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            writerThreadPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
        }
    }

    static void stop() {
        if (running.compareAndSet(true, false)) {
            EagleEyeCoreUtils.shutdownThreadPool(rollerThreadPool, 0);
            EagleEye.selfLog("[INFO] StatLoggerController: roller ThreadPool shutdown successfully");

            for (StatLogger statLogger : statLoggers.values()) {
                new StatLogRollingTask(statLogger).run();
            }

            try {
                Thread.sleep(STAT_ENTRY_COOL_DOWN_MILLIS);
            } catch (InterruptedException e) {
                // quietly
            }

            EagleEyeCoreUtils.shutdownThreadPool(writerThreadPool, 2000);
            EagleEye.selfLog("[INFO] StatLoggerController: writer ThreadPool shutdown successfully");
        }
    }

    private StatLogController() {
    }
}
