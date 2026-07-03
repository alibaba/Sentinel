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

import java.util.logging.Handler;

import com.alibaba.csp.sentinel.log.Logger;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * JUL adapter for Sentinel {@link Logger} SPI.
 *
 * @author Eric Zhao
 * @since 1.7.2
 */
public class JavaLoggingAdapter extends BaseJulLogger implements Logger {

    private final String loggerName;
    private final String fileNamePattern;

    private final java.util.logging.Logger julLogger;
    private final Handler logHandler;

    public JavaLoggingAdapter(String loggerName, String fileNamePattern) {
        AssertUtil.assertNotBlank(loggerName, "loggerName cannot be blank");
        AssertUtil.assertNotBlank(fileNamePattern, "fileNamePattern cannot be blank");
        this.loggerName = loggerName;
        this.fileNamePattern = fileNamePattern;

        this.julLogger = java.util.logging.Logger.getLogger(loggerName);
        this.logHandler = makeLoggingHandler(fileNamePattern, julLogger);
    }

    @Override
    public void info(String format, Object... arguments) {
        log(julLogger, logHandler, Level.INFO, format, arguments);
    }

    @Override
    public void info(String msg, Throwable e) {
        log(julLogger, logHandler, Level.INFO, msg, e);
    }

    @Override
    public void warn(String format, Object... arguments) {
        log(julLogger, logHandler, Level.WARNING, format, arguments);
    }

    @Override
    public void warn(String msg, Throwable e) {
        log(julLogger, logHandler, Level.WARNING, msg, e);
    }

    @Override
    public void trace(String format, Object... arguments) {
        log(julLogger, logHandler, Level.TRACE, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable e) {
        log(julLogger, logHandler, Level.TRACE, msg, e);
    }

    @Override
    public void debug(String format, Object... arguments) {
        log(julLogger, logHandler, Level.DEBUG, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable e) {
        log(julLogger, logHandler, Level.DEBUG, msg, e);
    }

    @Override
    public void error(String format, Object... arguments) {
        log(julLogger, logHandler, Level.ERROR, format, arguments);
    }

    @Override
    public void error(String msg, Throwable e) {
        log(julLogger, logHandler, Level.ERROR, msg, e);
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getFileNamePattern() {
        return fileNamePattern;
    }
}
