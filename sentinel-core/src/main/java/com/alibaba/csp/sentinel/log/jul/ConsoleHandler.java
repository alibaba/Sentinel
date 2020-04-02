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
package com.alibaba.csp.sentinel.log.jul;

import java.io.UnsupportedEncodingException;
import java.util.logging.*;

/**
 * This Handler publishes log records to console by using {@link java.util.logging.StreamHandler}.
 *
 * Print log of WARNING level or above to System.err,
 * and print log of INFO level or below to System.out.
 *
 * To use this handler, add the following VM argument:
 * <pre>
 * -Dcsp.sentinel.log.output.type=console
 * </pre>
 *
 * @author cdfive
 */
class ConsoleHandler extends Handler {

    /**
     * A Handler which publishes log records to System.out.
     */
    private StreamHandler stdoutHandler;

    /**
     * A Handler which publishes log records to System.err.
     */
    private StreamHandler stderrHandler;

    public ConsoleHandler() {
        this.stdoutHandler = new StreamHandler(System.out, new CspFormatter());
        this.stderrHandler = new StreamHandler(System.err, new CspFormatter());
    }

    @Override
    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        this.stdoutHandler.setFormatter(newFormatter);
        this.stderrHandler.setFormatter(newFormatter);
    }

    @Override
    public synchronized void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
        this.stdoutHandler.setEncoding(encoding);
        this.stderrHandler.setEncoding(encoding);
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            stderrHandler.publish(record);
            stderrHandler.flush();
        } else {
            stdoutHandler.publish(record);
            stdoutHandler.flush();
        }
    }

    @Override
    public void flush() {
        stdoutHandler.flush();
        stderrHandler.flush();
    }

    @Override
    public void close() throws SecurityException {
        stdoutHandler.close();
        stderrHandler.close();
    }
}
