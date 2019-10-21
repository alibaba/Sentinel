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

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;

/**
 * Implementation of commons-logging Log interface that delegates all
 * logging calls to the Avalon logging abstraction: the Logger interface.
 * <p>
 * There are two ways in which this class can be used:
 * <ul>
 * <li>the instance can be constructed with an Avalon logger
 * (by calling {@link #AvalonLogger(Logger)}). In this case, it acts
 * as a simple thin wrapping implementation over the logger. This is
 * particularly useful when using a property setter.
 * </li>
 * <li>the {@link #setDefaultLogger} class property can be called which
 * sets the ancestral Avalon logger for this class. Any <code>AvalonLogger</code>
 * instances created through the <code>LogFactory</code> mechanisms will output
 * to child loggers of this <code>Logger</code>.
 * </li>
 * </ul>
 * <p>
 * <strong>Note:</strong> <code>AvalonLogger</code> does not implement Serializable
 * because the constructors available for it make this impossible to achieve in all
 * circumstances; there is no way to "reconnect" to an underlying Logger object on
 * deserialization if one was just passed in to the constructor of the original
 * object. This class <i>was</i> marked Serializable in the 1.0.4 release of
 * commons-logging, but this never actually worked (a NullPointerException would
 * be thrown as soon as the deserialized object was used), so removing this marker
 * is not considered to be an incompatible change.
 *
 * @version $Id: AvalonLogger.java 1435115 2013-01-18 12:40:19Z tn $
 */
public class AvalonLogger implements Log {

    /** Ancestral Avalon logger. */
    private static volatile Logger defaultLogger = null;
    /** Avalon logger used to perform log. */
    private final transient Logger logger;

    /**
     * Constructs an <code>AvalonLogger</code> that outputs to the given
     * <code>Logger</code> instance.
     *
     * @param logger the Avalon logger implementation to delegate to
     */
    public AvalonLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Constructs an <code>AvalonLogger</code> that will log to a child
     * of the <code>Logger</code> set by calling {@link #setDefaultLogger}.
     *
     * @param name the name of the avalon logger implementation to delegate to
     */
    public AvalonLogger(String name) {
        if (defaultLogger == null) {
            throw new NullPointerException("default logger has to be specified if this constructor is used!");
        }
        this.logger = defaultLogger.getChildLogger(name);
    }

    /**
     * Gets the Avalon logger implementation used to perform logging.
     *
     * @return avalon logger implementation
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Sets the ancestral Avalon logger from which the delegating loggers will descend.
     *
     * @param logger the default avalon logger,
     * in case there is no logger instance supplied in constructor
     */
    public static void setDefaultLogger(Logger logger) {
        defaultLogger = logger;
    }

    /**
    * Logs a message with <code>org.apache.avalon.framework.logger.Logger.debug</code>.
    *
    * @param message to log
    * @param t log this cause
    * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable t) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.debug</code>.
     *
     * @param message to log.
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(Object message) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.error</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(Object message, Throwable t) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.error</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(Object message) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.fatalError</code>.
     *
     * @param message to log.
     * @param t log this cause.
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(Object message, Throwable t) {
        if (getLogger().isFatalErrorEnabled()) {
            getLogger().fatalError(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.fatalError</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(Object message) {
        if (getLogger().isFatalErrorEnabled()) {
            getLogger().fatalError(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.info</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(Object message, Throwable t) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.info</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(Object message) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info(String.valueOf(message));
        }
    }

    /**
     * Is logging to <code>org.apache.avalon.framework.logger.Logger.debug</code> enabled?
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * Is logging to <code>org.apache.avalon.framework.logger.Logger.error</code> enabled?
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return getLogger().isErrorEnabled();
    }

    /**
     * Is logging to <code>org.apache.avalon.framework.logger.Logger.fatalError</code> enabled?
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return getLogger().isFatalErrorEnabled();
    }

    /**
     * Is logging to <code>org.apache.avalon.framework.logger.Logger.info</code> enabled?
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    /**
     * Is logging to <code>org.apache.avalon.framework.logger.Logger.debug</code> enabled?
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * Is logging to <code>org.apache.avalon.framework.logger.Logger.warn</code> enabled?
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return getLogger().isWarnEnabled();
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.debug</code>.
     *
     * @param message to log.
     * @param t log this cause.
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(Object message, Throwable t) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.debug</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    public void trace(Object message) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String.valueOf(message));
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.warn</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(Object message, Throwable t) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with <code>org.apache.avalon.framework.logger.Logger.warn</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(Object message) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(String.valueOf(message));
        }
    }
}
