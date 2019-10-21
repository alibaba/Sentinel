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

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class EagleEyeLogDaemon implements Runnable {

    private static final long LOG_CHECK_INTERVAL = TimeUnit.SECONDS.toMillis(20);

    private static AtomicBoolean running = new AtomicBoolean(false);

    private static Thread worker = null;

    private static final CopyOnWriteArrayList<EagleEyeAppender> watchedAppenders
        = new CopyOnWriteArrayList<EagleEyeAppender>();

    static EagleEyeAppender watch(EagleEyeAppender appender) {
        watchedAppenders.addIfAbsent(appender);
        return appender;
    }

    static boolean unwatch(EagleEyeAppender appender) {
        return watchedAppenders.remove(appender);
    }

    @Override
    public void run() {
        while (running.get()) {

            cleanupFiles();

            try {
                Thread.sleep(LOG_CHECK_INTERVAL);
            } catch (InterruptedException e) {

            }

            flushAndReload();
        }
    }

    private void cleanupFiles() {
        for (EagleEyeAppender watchedAppender : watchedAppenders) {
            try {
                watchedAppender.cleanup();
            } catch (Exception e) {
                EagleEye.selfLog("[ERROR] fail to cleanup: " + watchedAppender, e);
            }
        }
        try {
            EagleEye.selfAppender.cleanup();
        } catch (Exception e) {
            // quietly
        }
    }

    private void flushAndReload() {
        for (EagleEyeAppender watchedAppender : watchedAppenders) {
            try {
                watchedAppender.reload();
            } catch (Exception e) {
                EagleEye.selfLog("[ERROR] fail to reload: " + watchedAppender, e);
            }
        }
        try {
            EagleEye.selfAppender.reload();
        } catch (Exception e) {
            // quietly
        }
    }

    static void start() {
        if (running.compareAndSet(false, true)) {
            Thread worker = new Thread(new EagleEyeLogDaemon());
            worker.setDaemon(true);
            worker.setName("EagleEye-LogDaemon-Thread");
            worker.start();
            EagleEyeLogDaemon.worker = worker;
        }
    }

    static void stop() {
        if (running.compareAndSet(true, false)) {

            closeAppenders();

            final Thread worker = EagleEyeLogDaemon.worker;
            if (worker != null) {
                try {
                    worker.interrupt();
                } catch (Exception e) {
                    // ignore
                }
                try {
                    worker.join(1000);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private static void closeAppenders() {
        for (EagleEyeAppender watchedAppender : watchedAppenders) {
            try {
                watchedAppender.close();
            } catch (Exception e) {
                EagleEye.selfLog("[ERROR] fail to close: " + watchedAppender, e);
            }
        }
    }

    static void flushAndWait() {
        for (EagleEyeAppender watchedAppender : watchedAppenders) {
            try {
                watchedAppender.flush();
            } catch (Exception e) {
                EagleEye.selfLog("[ERROR] fail to flush: " + watchedAppender, e);
            }
        }
    }

    private EagleEyeLogDaemon() {}
}
