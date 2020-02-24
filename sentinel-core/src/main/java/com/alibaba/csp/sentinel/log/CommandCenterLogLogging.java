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
package com.alibaba.csp.sentinel.log;

import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Default logger implementation.
 * @author xue8
 */
public class CommandCenterLogLogging extends LogBase implements com.alibaba.csp.sentinel.log.Logger {
    private final Logger heliumRecordLog = Logger.getLogger("cspCommandCenterLog");
    private final String FILE_NAME = "command-center.log";
    private final Handler logHandler = makeLogger(FILE_NAME, heliumRecordLog);

    @Override
    public void info(String format, Object... arguments) {
        log(heliumRecordLog, logHandler, Level.INFO, format, arguments);
    }

    @Override
    public void info(String msg, Throwable e) {
        log(heliumRecordLog, logHandler, Level.INFO, msg, e);
    }

    @Override
    public void warn(String format, Object... arguments) {
        log(heliumRecordLog, logHandler, Level.WARNING, format, arguments);
    }

    @Override
    public void warn(String msg, Throwable e) {
        log(heliumRecordLog, logHandler, Level.WARNING, msg, e);
    }

    @Override
    public void trace(String format, Object... arguments) {
        log(heliumRecordLog, logHandler, Level.TRACE, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable e) {
        log(heliumRecordLog, logHandler, Level.TRACE, msg, e);
    }

    @Override
    public void debug(String format, Object... arguments) {
        log(heliumRecordLog, logHandler, Level.DEBUG, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable e) {
        log(heliumRecordLog, logHandler, Level.DEBUG, msg, e);
    }

    @Override
    public void error(String format, Object... arguments) {
        log(heliumRecordLog, logHandler, Level.ERROR, format, arguments);
    }

    @Override
    public void error(String msg, Throwable e) {
        log(heliumRecordLog, logHandler, Level.ERROR, msg, e);
    }
}
