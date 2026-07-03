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

package com.alibaba.csp.sentinel.logging.slf4j;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author jason
 */
public class RecordLogTest extends AbstraceSlf4jLogTest {

    @Override
    protected String getLoggerName() {
        return RecordLog.LOGGER_NAME;
    }

    @Override
    protected void debug(String msg, Object... args) {
        RecordLog.debug(msg, args);
    }

    @Override
    protected void trace(String msg, Object... args) {
        RecordLog.trace(msg, args);
    }

    @Override
    protected void info(String msg, Object... args) {
        RecordLog.info(msg, args);
    }

    @Override
    protected void warn(String msg, Object... args) {
        RecordLog.warn(msg, args);
    }

    @Override
    protected void error(String msg, Object... args) {
        RecordLog.error(msg, args);
    }
}
