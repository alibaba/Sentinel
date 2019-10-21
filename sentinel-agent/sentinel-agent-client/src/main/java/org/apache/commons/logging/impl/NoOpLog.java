/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.logging.impl;

import java.io.Serializable;
import org.apache.commons.logging.Log;

/**
 * Trivial implementation of Log that throws away all messages.  No
 * configurable system properties are supported.
 *
 * @version $Id: NoOpLog.java 1432663 2013-01-13 17:24:18Z tn $
 */
public class NoOpLog implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 561423906191706148L;

    /** Convenience constructor */
    public NoOpLog() { }
    /** Base constructor */
    public NoOpLog(String name) { }
    /** Do nothing */
    public void trace(Object message) { }
    /** Do nothing */
    public void trace(Object message, Throwable t) { }
    /** Do nothing */
    public void debug(Object message) { }
    /** Do nothing */
    public void debug(Object message, Throwable t) { }
    /** Do nothing */
    public void info(Object message) { }
    /** Do nothing */
    public void info(Object message, Throwable t) { }
    /** Do nothing */
    public void warn(Object message) { }
    /** Do nothing */
    public void warn(Object message, Throwable t) { }
    /** Do nothing */
    public void error(Object message) { }
    /** Do nothing */
    public void error(Object message, Throwable t) { }
    /** Do nothing */
    public void fatal(Object message) { }
    /** Do nothing */
    public void fatal(Object message, Throwable t) { }

    /**
     * Debug is never enabled.
     *
     * @return false
     */
    public final boolean isDebugEnabled() { return false; }

    /**
     * Error is never enabled.
     *
     * @return false
     */
    public final boolean isErrorEnabled() { return false; }

    /**
     * Fatal is never enabled.
     *
     * @return false
     */
    public final boolean isFatalEnabled() { return false; }

    /**
     * Info is never enabled.
     *
     * @return false
     */
    public final boolean isInfoEnabled() { return false; }

    /**
     * Trace is never enabled.
     *
     * @return false
     */
    public final boolean isTraceEnabled() { return false; }

    /**
     * Warn is never enabled.
     *
     * @return false
     */
    public final boolean isWarnEnabled() { return false; }
}
