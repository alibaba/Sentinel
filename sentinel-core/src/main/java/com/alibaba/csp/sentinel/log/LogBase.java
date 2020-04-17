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
import java.util.Properties;

import static com.alibaba.csp.sentinel.util.ConfigUtil.addSeparator;

/**
 * <p>The base config class for logging.</p>
 *
 * <p>
 * The default log base directory is {@code ${user.home}/logs/csp/}. We can use the {@link #LOG_DIR}
 * property to override it. The default log file name dose not contain pid, but if multi-instances of the same service
 * are running in the same machine, we may want to distinguish the log file by process ID number.
 * In this case, {@link #LOG_NAME_USE_PID} property could be configured as "true" to turn on this switch.
 * </p>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
public class LogBase {

    public static final String LOG_DIR = "csp.sentinel.log.dir";
    public static final String LOG_NAME_USE_PID = "csp.sentinel.log.use.pid";
    public static final String LOG_OUTPUT_TYPE = "csp.sentinel.log.output.type";
    public static final String LOG_CHARSET = "csp.sentinel.log.charset";

    /**
     * Output biz log (e.g. RecordLog and CommandCenterLog) to file.
     */
    public static final String LOG_OUTPUT_TYPE_FILE = "file";
    /**
     * Output biz log (e.g. RecordLog and CommandCenterLog) to console.
     */
    public static final String LOG_OUTPUT_TYPE_CONSOLE = "console";
    public static final String LOG_CHARSET_UTF8 = "utf-8";

    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";


    private static boolean logNameUsePid;
    private static String logOutputType;
    private static String logBaseDir;
    private static String logCharSet;

    static {
        try {
            initializeDefault();
            loadProperties();
        } catch (Throwable t) {
            System.err.println("[LogBase] FATAL ERROR when initializing logging config");
            t.printStackTrace();
        }
    }

    private static void initializeDefault() {
        logNameUsePid = false;
        logOutputType = LOG_OUTPUT_TYPE_FILE;
        logBaseDir = addSeparator(System.getProperty(USER_HOME)) + DIR_NAME + File.separator;
        logCharSet = LOG_CHARSET_UTF8;
    }

    private static void loadProperties() {
        Properties properties = LogConfigLoader.getProperties();

        logOutputType = properties.get(LOG_OUTPUT_TYPE) == null ? logOutputType : properties.getProperty(LOG_OUTPUT_TYPE);
        if (!LOG_OUTPUT_TYPE_FILE.equalsIgnoreCase(logOutputType) && !LOG_OUTPUT_TYPE_CONSOLE.equalsIgnoreCase(logOutputType)) {
            logOutputType = LOG_OUTPUT_TYPE_FILE;
        }
        System.out.println("INFO: Sentinel log output type is: " + logOutputType);

        logCharSet = properties.getProperty(LOG_CHARSET) == null ? logCharSet : properties.getProperty(LOG_CHARSET);
        System.out.println("INFO: Sentinel log charset is: " + logCharSet);


        logBaseDir = properties.getProperty(LOG_DIR) == null ? logBaseDir : properties.getProperty(LOG_DIR);
        logBaseDir = addSeparator(logBaseDir);
        File dir = new File(logBaseDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("ERROR: create Sentinel log base directory error: " + logBaseDir);
            }
        }
        System.out.println("INFO: Sentinel log base directory is: " + logBaseDir);

        String usePid = properties.getProperty(LOG_NAME_USE_PID);
        logNameUsePid = "true".equalsIgnoreCase(usePid);
        System.out.println("INFO: Sentinel log name use pid is: " + logNameUsePid);
    }


    /**
     * Whether log file name should contain pid. This switch is configured by {@link #LOG_NAME_USE_PID} system property.
     *
     * @return true if log file name should contain pid, return true, otherwise false
     */
    public static boolean isLogNameUsePid() {
        return logNameUsePid;
    }

    /**
     * Get the log file base directory path, which is guaranteed ended with {@link File#separator}.
     *
     * @return log file base directory path
     */
    public static String getLogBaseDir() {
        return logBaseDir;
    }

    /**
     * Get the log file output type.
     *
     * @return log output type, "file" by default
     */
    public static String getLogOutputType() {
        return logOutputType;
    }

    /**
     * Get the log file charset.
     *
     * @return the log file charset, "utf-8" by default
     */
    public static String getLogCharset() {
        return logCharSet;
    }

}
