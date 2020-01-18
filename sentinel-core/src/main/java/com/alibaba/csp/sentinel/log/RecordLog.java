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

import java.util.Iterator;
import java.util.ServiceLoader;

/***
 * The basic logger for vital events.
 *
 * @author youji.zj
 */
public class RecordLog extends LogBase {

    private static com.alibaba.csp.sentinel.log.Logger log = null;

    static {
        ServiceLoader<Logger> load = ServiceLoader.load(Logger.class);
        Logger logger = null;
        Iterator<Logger> iterator = load.iterator();
        while (iterator.hasNext()) {
            Logger next = iterator.next();
            LogTarget annotation = next.getClass().getAnnotation(LogTarget.class);
            if (annotation == null) {
                continue;
            }
            String value = annotation.value().name();
            if (value.equals(LogType.RECORD_LOG.name())) {
                logger = next;
                break;
            }
        }
        // Use user implementations.
        if (logger != null) {
            log = logger;
        } else {
            // Use default.
            log = new RecordLogLogging();
        }
    }

    public static void info(String detail, Object... params) {
        log.info(detail, params);
    }

    public static void info(String detail, Throwable e) {
        log.info(detail, e);
    }

    public static void warn(String detail, Object... params) {
        log.warn(detail, params);
    }

    public static void warn(String detail, Throwable e) {
        log.warn(detail, e);
    }

}
