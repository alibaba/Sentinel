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
    private static final Logger heliumRecordLog = Logger.getLogger("cspSentinelRecordLog");
    private static final String FILE_NAME = "sentinel-record.log";
    private static Handler logHandler = null;

    static {
        logHandler = makeLogger(FILE_NAME, heliumRecordLog);
    }

    public static void info(String detail, Object... params) {
        log(heliumRecordLog, logHandler, Level.INFO, detail, params);
    }

    public static void info(String detail, Throwable e) {
        log(heliumRecordLog, logHandler, Level.INFO, detail, e);
    }

    public static void warn(String detail, Object... params) {
        log(heliumRecordLog, logHandler, Level.WARNING, detail, params);
    }

    public static void warn(String detail, Throwable e) {
        log(heliumRecordLog, logHandler, Level.WARNING, detail, e);
    }
}
