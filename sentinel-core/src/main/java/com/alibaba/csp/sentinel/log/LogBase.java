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

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.csp.sentinel.util.PidUtil;

/**
 * Default log base dir is ${user.home}/logs/csp/, we can use {@link #LOG_DIR} System property to override it.
 * Default log file name dose not contain pid, but if multi instances of the same app are running in the same
 * machine, we may want to distinguish the log file by pid number, in this case, {@link #LOG_NAME_USE_PID}
 * System property could be configured as "true" to turn on this switch.
 *
 * @author leyou
 */
public class LogBase {
    public static final String LOG_CHARSET = "utf-8";
    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";
    public static final String LOG_DIR = "csp.sentinel.log.dir";
    public static final String LOG_NAME_USE_PID = "csp.sentinel.log.use.pid";
    private static boolean logNameUsePid = false;

    private static String logBaseDir;

    static {
        try {
            init();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    private static void init() {
        // first use -D, then use user home.
        String logDir = System.getProperty(LOG_DIR);

        if (logDir == null || logDir.isEmpty()) {
            logDir = System.getProperty(USER_HOME);
            logDir = addSeparator(logDir) + DIR_NAME + File.separator;
        }
        logDir = addSeparator(logDir);
        File dir = new File(logDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("ERROR: create log base dir error: " + logDir);
            }
        }
        // logBaseDir must end with File.separator
        logBaseDir = logDir;
        System.out.println("INFO: log base dir is: " + logBaseDir);

        String usePid = System.getProperty(LOG_NAME_USE_PID, "");
        logNameUsePid = "true".equalsIgnoreCase(usePid);
        System.out.println("INFO: log name use pid is: " + logNameUsePid);
    }

    /**
     * Whether log file name should contain pid. This switch is configured by {@link #LOG_NAME_USE_PID} System property.
     *
     * @return if log file name should contain pid, return true, otherwise return false.
     */
    public static boolean isLogNameUsePid() {
        return logNameUsePid;
    }

    private static String addSeparator(String logDir) {
        if (!logDir.endsWith(File.separator)) {
            logDir += File.separator;
        }
        return logDir;
    }

    protected static void log(Logger logger, Handler handler, Level level, String detail, Object... params) {
        if (detail == null) {
            return;
        }
        LoggerUtils.disableOtherHandlers(logger, handler);
        if (params.length == 0) {
            logger.log(level, detail);
        } else {
            logger.log(level, detail, params);
        }
    }

    protected static void log(Logger logger, Handler handler, Level level, String detail, Throwable throwable) {
        if (detail == null) {
            return;
        }
        LoggerUtils.disableOtherHandlers(logger, handler);
        logger.log(level, detail, throwable);
    }

    /**
     * Get log file base directory path, the returned path is guaranteed end with {@link File#separator}
     *
     * @return log file base directory path.
     */
    public static String getLogBaseDir() {
        return logBaseDir;
    }

    protected static Handler makeLogger(String logName, Logger heliumRecordLog) {
        CspFormatter formatter = new CspFormatter();
        String fileName = LogBase.getLogBaseDir() + logName;
        if (isLogNameUsePid()) {
            fileName += ".pid" + PidUtil.getPid();
        }
        Handler handler = null;
        try {
            handler = new DateFileLogHandler(fileName + ".%d", 1024 * 1024 * 200, 4, true);
            handler.setFormatter(formatter);
            handler.setEncoding(LOG_CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (handler != null) {
            LoggerUtils.disableOtherHandlers(heliumRecordLog, handler);
        }
        heliumRecordLog.setLevel(Level.ALL);
        return handler;
    }
}
