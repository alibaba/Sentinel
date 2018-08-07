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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * The basic logger for vital events.
 *
 * @author youji.zj
 */
public class RecordLog extends LogBase {
    private static final Logger heliumRecordLog = Logger.getLogger("cspRecordLog");
    private static final String FILE_NAME = "record.log";
    private static Handler logHandler = null;

    static {
        logHandler = makeLogger(FILE_NAME, heliumRecordLog);
    }

    /**
     * Change log dir, the dir will be created if not exits
     */
    public static void resetLogBaseDir(String baseDir) {
        setLogBaseDir(baseDir);
        logHandler = makeLogger(FILE_NAME, heliumRecordLog);
    }

    public static void info(String detail) {
        LoggerUtils.disableOtherHandlers(heliumRecordLog, logHandler);
        heliumRecordLog.log(Level.INFO, detail);
    }

    public static void info(String detail, Throwable e) {
        LoggerUtils.disableOtherHandlers(heliumRecordLog, logHandler);
        heliumRecordLog.log(Level.INFO, detail, e);
    }

    public static void warn(String detail) {
        LoggerUtils.disableOtherHandlers(heliumRecordLog, logHandler);
        heliumRecordLog.log(Level.WARNING, detail);
    }

    public static void warn(String detail, Throwable e) {
        LoggerUtils.disableOtherHandlers(heliumRecordLog, logHandler);
        heliumRecordLog.log(Level.WARNING, detail, e);
    }
}
