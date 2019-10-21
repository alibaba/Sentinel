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
import org.apache.log4j.Logger;
import org.apache.log4j.Hierarchy;
import org.apache.commons.logging.Log;

/**
 * Implementation of <code>org.apache.commons.logging.Log</code>
 * that wraps the <a href="http://avalon.apache.org/logkit/">avalon-logkit</a>
 * logging system. Configuration of <code>LogKit</code> is left to the user.
 * <p>
 * <code>LogKit</code> accepts only <code>String</code> messages.
 * Therefore, this implementation converts object messages into strings
 * by called their <code>toString()</code> method before logging them.
 *
 * @version $Id: LogKitLogger.java 1448119 2013-02-20 12:28:04Z tn $
 */
public class LogKitLogger implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 3768538055836059519L;

    // ------------------------------------------------------------- Attributes

    /** Logging goes to this <code>LogKit</code> logger */
    protected transient volatile Logger logger = null;

    /** Name of this logger */
    protected String name = null;

    // ------------------------------------------------------------ Constructor

    /**
     * Construct <code>LogKitLogger</code> which wraps the <code>LogKit</code>
     * logger with given name.
     *
     * @param name log name
     */
    public LogKitLogger(String name) {
        this.name = name;
        this.logger = getLogger();
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the underlying Logger we are using.
     */
    public Logger getLogger() {
        Logger result = logger;
        if (result == null) {
            synchronized(this) {
                result = logger;
                if (result == null) {
                   // logger = result = Hierarchy.getDefaultHierarchy().getLoggerFor(name);
                }
            }
        }
        return result;
    }

    // ----------------------------------------------------- Log Implementation

    /**
     * Logs a message with <code>org.apache.log.Priority.DEBUG</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
    */
    public void trace(Object message) {
        debug(message);
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.DEBUG</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(Object message, Throwable t) {
        debug(message, t);
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.DEBUG</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(Object message) {
        if (message != null) {
            getLogger().debug(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.DEBUG</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable t) {
        if (message != null) {
            getLogger().debug(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.INFO</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(Object message) {
        if (message != null) {
            getLogger().info(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.INFO</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(Object message, Throwable t) {
        if (message != null) {
            getLogger().info(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.WARN</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(Object message) {
        if (message != null) {
            getLogger().warn(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.WARN</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(Object message, Throwable t) {
        if (message != null) {
            getLogger().warn(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.ERROR</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(Object message) {
        if (message != null) {
            getLogger().error(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.ERROR</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(Object message, Throwable t) {
        if (message != null) {
            getLogger().error(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.FATAL_ERROR</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(Object message) {
        if (message != null) {
          //  getLogger().fatalError(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.log.Priority.FATAL_ERROR</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(Object message, Throwable t) {
        if (message != null) {
        //    getLogger().fatalError(String.valueOf(message), t);
        }
    }

    /**
     * Checks whether the <code>LogKit</code> logger will log messages of priority <code>DEBUG</code>.
     */
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * Checks whether the <code>LogKit</code> logger will log messages of priority <code>ERROR</code>.
     */
    public boolean isErrorEnabled() {
       //return getLogger().isErrorEnabled();
        return true;
    }

    /**
     * Checks whether the <code>LogKit</code> logger will log messages of priority <code>FATAL_ERROR</code>.
     */
    public boolean isFatalEnabled() {
      //  return getLogger().isFatalErrorEnabled();
        return true;
    }

    /**
     * Checks whether the <code>LogKit</code> logger will log messages of priority <code>INFO</code>.
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    /**
     * Checks whether the <code>LogKit</code> logger will log messages of priority <code>DEBUG</code>.
     */
    public boolean isTraceEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * Checks whether the <code>LogKit</code> logger will log messages of priority <code>WARN</code>.
     */
    public boolean isWarnEnabled() {
       // return getLogger().isWarnEnabled();
        return true;
    }
}
