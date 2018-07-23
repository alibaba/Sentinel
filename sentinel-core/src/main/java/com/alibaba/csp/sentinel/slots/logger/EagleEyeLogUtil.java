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
package com.alibaba.csp.sentinel.slots.logger;

import java.io.File;

import com.alibaba.csp.sentinel.eagleeye.EagleEye;
import com.alibaba.csp.sentinel.eagleeye.StatLogger;

public class EagleEyeLogUtil {

    private static final String DIR_NAME = "csp";
    private static final String FILE_NAME = "sentinel-block.log";

    private static StatLogger statLogger;

    static {
        String path = DIR_NAME + File.separator + FILE_NAME;

        statLogger = EagleEye.statLoggerBuilder("sentinel-block-record")
            .intervalSeconds(1)
            .entryDelimiter('|')
            .keyDelimiter(',')
            .valueDelimiter(',')
            .maxEntryCount(6000)
            .baseLogFilePath(path)
            .maxFileSizeMB(300)
            .maxBackupIndex(3)
            .buildSingleton();
    }

    public static void log(String resource, String exceptionName, String ruleLimitApp, String origin, int count) {
        statLogger.stat(resource, exceptionName, ruleLimitApp, origin).count(count);
    }
}
