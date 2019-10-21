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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;

/**
 * Implementation of the <code>org.apache.commons.logging.Log</code>
 * interface that wraps the standard JDK logging mechanisms that were
 * introduced in the Merlin release (JDK 1.4).
 *
 * @version $Id: Jdk14Logger.java 1448063 2013-02-20 10:01:41Z tn $
 */
public class Jdk14Logger implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 4784713551416303804L;

    /**
     * This member variable simply ensures that any attempt to initialise
     * this class in a pre-1.4 JVM will result in an ExceptionInInitializerError.
     * It must not be private, as an optimising compiler could detect that it
     * is not used and optimise it away.
     */
    protected static final Level dummyLevel = Level.FINE;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk14Logger(String name) {
        this.name = name;
        logger = getLogger();
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The underlying Logger implementation we are using.
     */
    protected transient Logger logger = null;

    /**
     * The name of the logger we are wrapping.
     */
    protected String name = null;

    // --------------------------------------------------------- Protected Methods

    protected void log( Level level, String msg, Throwable ex ) {
        Logger logger = getLogger();
        if (logger.isLoggable(level)) {
            // Hack (?) to get the stack trace.
            Throwable dummyException = new Throwable();
            StackTraceElement locations[] = dummyException.getStackTrace();
            // LOGGING-132: use the provided logger name instead of the class name
            String cname = name;
            String method = "unknown";
            // Caller will be the third element
            if( locations != null && locations.length > 2 ) {
                StackTraceElement caller = locations[2];
                method = caller.getMethodName();
            }
            if( ex == null ) {
                logger.logp( level, cname, method, msg );
            } else {
                logger.logp( level, cname, method, msg, ex );
            }
        }
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Logs a message with <code>java.util.logging.Level.FINE</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.FINE</code>.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Return the native Logger instance we are using.
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(name);
        }
        return logger;
    }

    /**
     * Logs a message with <code>java.util.logging.Level.INFO</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.INFO</code>.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }

    /**
     * Is debug logging currently enabled?
     */
    public boolean isDebugEnabled() {
        return getLogger().isLoggable(Level.FINE);
    }

    /**
     * Is error logging currently enabled?
     */
    public boolean isErrorEnabled() {
        return getLogger().isLoggable(Level.SEVERE);
    }

    /**
     * Is fatal logging currently enabled?
     */
    public boolean isFatalEnabled() {
        return getLogger().isLoggable(Level.SEVERE);
    }

    /**
     * Is info logging currently enabled?
     */
    public boolean isInfoEnabled() {
        return getLogger().isLoggable(Level.INFO);
    }

    /**
     * Is trace logging currently enabled?
     */
    public boolean isTraceEnabled() {
        return getLogger().isLoggable(Level.FINEST);
    }

    /**
     * Is warn logging currently enabled?
     */
    public boolean isWarnEnabled() {
        return getLogger().isLoggable(Level.WARNING);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.FINEST</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.FINEST</code>.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.WARNING</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.WARNING</code>.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }
}
