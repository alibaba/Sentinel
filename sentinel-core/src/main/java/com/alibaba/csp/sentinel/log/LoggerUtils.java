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
import java.util.logging.Logger;

/**
 * Util class for logger.
 */
class LoggerUtils {

    /**
     * Remove all current handlers from the logger and attach it with the given log handler.
     *
     * @param logger  logger
     * @param handler the log handler
     */
    static void disableOtherHandlers(Logger logger, Handler handler) {
        if (logger == null) {
            return;
        }

        synchronized (logger) {
            Handler[] handlers = logger.getHandlers();
            if (handlers == null) {
                return;
            }
            if (handlers.length == 1 && handlers[0].equals(handler)) {
                return;
            }

            logger.setUseParentHandlers(false);
            // Remove all current handlers.
            for (Handler h : handlers) {
                logger.removeHandler(h);
            }
            // Attach the given handler.
            logger.addHandler(handler);
        }
    }
}
