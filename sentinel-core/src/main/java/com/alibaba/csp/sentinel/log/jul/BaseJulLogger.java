/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.log.jul;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.util.PidUtil;

import static com.alibaba.csp.sentinel.log.LogBase.LOG_OUTPUT_TYPE_CONSOLE;
import static com.alibaba.csp.sentinel.log.LogBase.LOG_OUTPUT_TYPE_FILE;

/**
 * The default logger based on java.util.logging.
 *
 * @author Eric Zhao
 * @since 1.7.2
 */
public class BaseJulLogger {

    protected void log(Logger logger, Handler handler, Level level, String detail, Object... params) {
        if (detail == null) {
            return;
        }
        disableOtherHandlers(logger, handler);

        // Compatible with slf4j placeholder format "{}".
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(detail, params);
        String message = formattingTuple.getMessage();
        logger.log(level, message);
    }

    protected void log(Logger logger, Handler handler, Level level, String detail, Throwable throwable) {
        if (detail == null) {
            return;
        }
        disableOtherHandlers(logger, handler);
        logger.log(level, detail, throwable);
    }

    protected Handler makeLoggingHandler(String logName, Logger heliumRecordLog) {
        CspFormatter formatter = new CspFormatter();
        String logCharSet = LogBase.getLogCharset();
        Handler handler = null;

        // Create handler according to logOutputType, set formatter to CspFormatter, set encoding to LOG_CHARSET
        switch (LogBase.getLogOutputType()) {
            case LOG_OUTPUT_TYPE_FILE:
                String fileName = LogBase.getLogBaseDir() + logName;
                if (LogBase.isLogNameUsePid()) {
                    fileName += ".pid" + PidUtil.getPid();
                }
                try {
                    handler = new DateFileLogHandler(fileName + ".%d", 1024 * 1024 * 200, 4, true);
                    handler.setFormatter(formatter);
                    handler.setEncoding(logCharSet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case LOG_OUTPUT_TYPE_CONSOLE:
                try {
                    handler = new ConsoleHandler();
                    handler.setFormatter(formatter);
                    handler.setEncoding(logCharSet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        if (handler != null) {
            disableOtherHandlers(heliumRecordLog, handler);
        }

        // Set log level to INFO by default
        heliumRecordLog.setLevel(Level.INFO);
        return handler;
    }

    /**
     * Remove all current handlers from the logger and attach it with the given log handler.
     *
     * @param logger  logger
     * @param handler the log handler
     */
    static void disableOtherHandlers(Logger logger, Handler handler) {
        if (logger == null) {
            return;
        }

        synchronized (logger) {
            Handler[] handlers = logger.getHandlers();
            if (handlers == null) {
                return;
            }
            if (handlers.length == 1 && handlers[0].equals(handler)) {
                return;
            }

            logger.setUseParentHandlers(false);
            // Remove all current handlers.
            for (Handler h : handlers) {
                logger.removeHandler(h);
            }
            // Attach the given handler.
            logger.addHandler(handler);
        }
    }
}
