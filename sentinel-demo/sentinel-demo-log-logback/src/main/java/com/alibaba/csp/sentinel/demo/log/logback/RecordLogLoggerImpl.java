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
package com.alibaba.csp.sentinel.demo.log.logback;

import com.alibaba.csp.sentinel.log.LogTarget;
import com.alibaba.csp.sentinel.log.Logger;
import com.alibaba.csp.sentinel.log.RecordLog;

import org.slf4j.LoggerFactory;

/**
 * This class is a demo shows how to create a customized logger implementation.
 *
 * <ul>
 * <li>1. Create a class which implements the {@link Logger} SPI interface</li>
 * <li>2. Use a {@link LogTarget} to specify the log type</li>
 * <li>3. Implement your own method </li>
 * <li>4. Add your logger in {@code com.alibaba.csp.sentinel.log.Logger} file which is stored in
 * {@code resources/META-INF/services/} directory </li>
 * </ul>
 *
 * @author xue8
 */
@LogTarget(value = RecordLog.LOGGER_NAME)
public class RecordLogLoggerImpl implements Logger {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(RecordLog.LOGGER_NAME);

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable e) {
        logger.info(msg, e);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable e) {
        logger.warn(msg, e);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable e) {
        logger.trace(msg, e);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable e) {
        logger.debug(msg, e);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable e) {
        logger.error(msg, e);
    }

}
