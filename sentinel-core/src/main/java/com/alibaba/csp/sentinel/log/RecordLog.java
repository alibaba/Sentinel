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
package com.alibaba.csp.sentinel.log;

import com.alibaba.csp.sentinel.log.jul.JavaLoggingAdapter;

/**
 * The basic biz logger of Sentinel.
 *
 * @author youji.zj
 * @author Eric Zhao
 */
public class RecordLog {

    public static final String LOGGER_NAME = "sentinelRecordLogger";
    public static final String DEFAULT_LOG_FILENAME = "sentinel-record.log";

    private static com.alibaba.csp.sentinel.log.Logger logger = null;

    static {
        try {
            // Load user-defined logger implementation first.
            logger = LoggerSpiProvider.getLogger(LOGGER_NAME);
            if (logger == null) {
                // If no customized loggers are provided, we use the default logger based on JUL.
                logger = new JavaLoggingAdapter(LOGGER_NAME, DEFAULT_LOG_FILENAME);
            }
        } catch (Throwable t) {
            System.err.println("Error: failed to initialize Sentinel RecordLog");
            t.printStackTrace();
        }
    }

    public static void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    public static void info(String msg, Throwable e) {
        logger.info(msg, e);
    }

    public static void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    public static void warn(String msg, Throwable e) {
        logger.warn(msg, e);
    }

    public static void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    public static void trace(String msg, Throwable e) {
        logger.trace(msg, e);
    }

    public static void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    public static void debug(String msg, Throwable e) {
        logger.debug(msg, e);
    }

    public static void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    public static void error(String msg, Throwable e) {
        logger.error(msg, e);
    }

    private RecordLog() {}
}
