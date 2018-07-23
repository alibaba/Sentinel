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
package com.alibaba.csp.sentinel.eagleeye;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public final class EagleEye {

    public static final String CLASS_LOCATION = getEagleEyeLocation();

    static final String USER_HOME = locateUserHome();

    static final String BASE_LOG_DIR = locateBaseLogPath();

    static final String EAGLEEYE_LOG_DIR = locateEagleEyeLogPath();

    static final String APP_LOG_DIR = locateAppLogPath();

    static final Charset DEFAULT_CHARSET = getDefaultOutputCharset();

    static final String EAGLEEYE_SELF_LOG_FILE = EagleEye.EAGLEEYE_LOG_DIR + "eagleeye-self.log";

    // 200MB
    static final long MAX_SELF_LOG_FILE_SIZE = 200 * 1024 * 1024;

    static EagleEyeAppender selfAppender = createSelfLogger();

    static private TokenBucket exceptionBucket = new TokenBucket(10, TimeUnit.SECONDS.toMillis(10));

    static String getEagleEyeLocation() {
        try {
            URL resource = EagleEye.class.getProtectionDomain().getCodeSource().getLocation();
            if (resource != null) {
                return resource.toString();
            }
        } catch (Throwable t) {
            // ignore
        }
        return "unknown location";
    }

    static Charset getDefaultOutputCharset() {
        Charset cs;
        String charsetName = EagleEyeCoreUtils.getSystemProperty("EAGLEEYE.CHARSET");
        if (EagleEyeCoreUtils.isNotBlank(charsetName)) {
            charsetName = charsetName.trim();
            try {
                cs = Charset.forName(charsetName);
                if (cs != null) {
                    return cs;
                }
            } catch (Exception e) {
                // quietly
            }
        }
        try {
            cs = Charset.forName("GB18030");
        } catch (Exception e) {
            try {
                cs = Charset.forName("GBK");
            } catch (Exception e2) {
                cs = Charset.forName("UTF-8");
            }
        }
        return cs;
    }

    private static String locateUserHome() {
        String userHome = EagleEyeCoreUtils.getSystemProperty("user.home");
        if (EagleEyeCoreUtils.isNotBlank(userHome)) {
            if (!userHome.endsWith(File.separator)) {
                userHome += File.separator;
            }
        } else {
            userHome = "/tmp/";
        }
        return userHome;
    }

    private static String locateBaseLogPath() {
        String tmpPath = EagleEyeCoreUtils.getSystemProperty("JM.LOG.PATH");
        if (EagleEyeCoreUtils.isNotBlank(tmpPath)) {
            if (!tmpPath.endsWith(File.separator)) {
                tmpPath += File.separator;
            }
        } else {
            tmpPath = USER_HOME + "logs" + File.separator;
        }
        return tmpPath;
    }

    private static String locateEagleEyeLogPath() {
        String tmpPath = EagleEyeCoreUtils.getSystemProperty("EAGLEEYE.LOG.PATH");
        if (EagleEyeCoreUtils.isNotBlank(tmpPath)) {
            if (!tmpPath.endsWith(File.separator)) {
                tmpPath += File.separator;
            }
        } else {
            tmpPath = BASE_LOG_DIR + "eagleeye" + File.separator;
        }
        return tmpPath;
    }

    private static String locateAppLogPath() {
        String appName = EagleEyeCoreUtils.getSystemProperty("project.name");
        if (EagleEyeCoreUtils.isNotBlank(appName)) {
            return USER_HOME + appName + File.separator + "logs" + File.separator;
        } else {
            return EAGLEEYE_LOG_DIR;
        }
    }

    static private final EagleEyeAppender createSelfLogger() {
        EagleEyeRollingFileAppender selfAppender = new EagleEyeRollingFileAppender(EAGLEEYE_SELF_LOG_FILE,
            EagleEyeCoreUtils.getSystemPropertyForLong("EAGLEEYE.LOG.SELF.FILESIZE", MAX_SELF_LOG_FILE_SIZE),
            false);
        return new SyncAppender(selfAppender);
    }

    static {
        initEagleEye();
    }

    private static void initEagleEye() {
        try {
            selfLog("[INFO] EagleEye started (" + CLASS_LOCATION + ")" + ", classloader="
                + EagleEye.class.getClassLoader());
        } catch (Throwable e) {
            selfLog("[INFO] EagleEye started (" + CLASS_LOCATION + ")");
        }

        try {
            EagleEyeLogDaemon.start();
        } catch (Throwable e) {
            selfLog("[ERROR] fail to start EagleEyeLogDaemon", e);
        }
        try {
            StatLogController.start();
        } catch (Throwable e) {
            selfLog("[ERROR] fail to start StatLogController", e);
        }

    }

    public static void shutdown() {
        selfLog("[WARN] EagleEye is shutting down (" + CLASS_LOCATION + ")");

        EagleEye.flush();

        try {
            StatLogController.stop();
            EagleEye.selfLog("[INFO] StatLogController stopped");
        } catch (Throwable e) {
            selfLog("[ERROR] fail to stop StatLogController", e);
        }

        try {
            EagleEyeLogDaemon.stop();
            EagleEye.selfLog("[INFO] EagleEyeLogDaemon stopped");
        } catch (Throwable e) {
            selfLog("[ERROR] fail to stop EagleEyeLogDaemon", e);
        }

        EagleEye.selfLog("[WARN] EagleEye shutdown successfully (" + CLASS_LOCATION + ")");
        try {
            selfAppender.close();
        } catch (Throwable e) {
            // ignore
        }
    }

    private EagleEye() {
    }

    static public StatLogger statLogger(String loggerName) {
        return statLoggerBuilder(loggerName).buildSingleton();
    }

    static public StatLoggerBuilder statLoggerBuilder(String loggerName) {
        return new StatLoggerBuilder(loggerName);
    }

    static void setEagelEyeSelfAppender(EagleEyeAppender appender) {
        selfAppender = appender;
    }

    public static void selfLog(String log) {
        try {
            String timestamp = EagleEyeCoreUtils.formatTime(System.currentTimeMillis());
            String line = "[" + timestamp + "] " + log + EagleEyeCoreUtils.NEWLINE;
            selfAppender.append(line);
        } catch (Throwable t) {
        }
    }

    public static void selfLog(String log, Throwable e) {
        long now = System.currentTimeMillis();
        if (exceptionBucket.accept(now)) {
            try {
                String timestamp = EagleEyeCoreUtils.formatTime(now);
                StringWriter sw = new StringWriter(4096);
                PrintWriter pw = new PrintWriter(sw, false);
                pw.append('[').append(timestamp).append("] ").append(log).append(EagleEyeCoreUtils.NEWLINE);
                e.printStackTrace(pw);
                pw.println();
                pw.flush();
                selfAppender.append(sw.toString());
            } catch (Throwable t) {
            }
        }
    }

    static public void flush() {
        EagleEyeLogDaemon.flushAndWait();
    }

}
