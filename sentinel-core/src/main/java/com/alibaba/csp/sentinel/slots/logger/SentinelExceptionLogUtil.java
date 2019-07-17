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
package com.alibaba.csp.sentinel.slots.logger;

import com.alibaba.csp.sentinel.eagleeye.EagleEye;
import com.alibaba.csp.sentinel.eagleeye.StatLogger;
import com.alibaba.csp.sentinel.log.LogBase;

/**
 * @author Eric Zhao
 * @since 1.6.3
 */
public final class SentinelExceptionLogUtil {

    public static final String LOG_FILE_NAME = "sentinel-biz-exception.log";

    private static StatLogger statLogger;

    static {
        String path = LogBase.getLogBaseDir() + LOG_FILE_NAME;

        statLogger = EagleEye.statLoggerBuilder("sentinel-biz-exception-logger")
            .intervalSeconds(10)
            .entryDelimiter('|')
            .keyDelimiter(',')
            .valueDelimiter(',')
            .maxEntryCount(5000)
            .configLogFilePath(path)
            .maxFileSizeMB(300)
            .maxBackupIndex(3)
            .buildSingleton();
    }

    public static void log(String resourceName, String exceptionName) {
        statLogger.stat(resourceName, exceptionName).count();
    }

    public static void log(String resourceName, String exceptionName, int count) {
        statLogger.stat(resourceName, exceptionName).count(count);
    }

    private SentinelExceptionLogUtil() {}
}
