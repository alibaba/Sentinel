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

/**
 * Logger for command center.
 */
public class CommandCenterLog {
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
            if (value.equals(LogType.COMMAND_CENTER_LOG.name())) {
                logger = next;
                break;
            }
        }
        // Use user implementations.
        if (logger != null) {
            log = logger;
        } else {
            // Use default implementations.
            log = new CommandCenterLogLogging();
        }
    }

    public static void info(String format, Object... arguments) {
        log.info(format, arguments);
    }

    public static void info(String msg, Throwable e) {
        log.info(msg, e);
    }

    public static void warn(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    public static void warn(String msg, Throwable e) {
        log.warn(msg, e);
    }

    public static void trace(String format, Object... arguments) {
        log.trace(format, arguments);
    }

    public static void trace(String msg, Throwable e) {
        log.trace(msg, e);
    }

    public static void debug(String format, Object... arguments) {
        log.debug(format, arguments);
    }

    public static void debug(String msg, Throwable e) {
        log.debug(msg, e);
    }

    public static void error(String format, Object... arguments) {
        log.error(format, arguments);
    }

    public static void error(String msg, Throwable e) {
        log.error(msg, e);
    }
}
