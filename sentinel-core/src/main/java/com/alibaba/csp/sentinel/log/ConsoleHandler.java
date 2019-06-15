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

/**
 * This <tt>Handler</tt> publishes log records to <tt>System.out</tt>.
 *
 * Extends from {@link java.util.logging.ConsoleHandler}, and set the OutputStream to System.out.
 *
 * To use this handler, add the following VM argument:
 * <pre>
 * -Dcsp.sentinel.log.type=console
 * </pre>
 *
 * @author cdfive
 */
class ConsoleHandler extends java.util.logging.ConsoleHandler {

    public ConsoleHandler() {
        super();
        setOutputStream(System.out);
    }
}
