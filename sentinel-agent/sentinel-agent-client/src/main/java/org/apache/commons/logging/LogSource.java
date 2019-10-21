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

package org.apache.commons.logging;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.apache.commons.logging.impl.NoOpLog;

/**
 * Factory for creating {@link Log} instances.  Applications should call
 * the <code>makeNewLogInstance()</code> method to instantiate new instances
 * of the configured {@link Log} implementation class.
 * <p>
 * By default, calling <code>getInstance()</code> will use the following
 * algorithm:
 * <ul>
 * <li>If Log4J is available, return an instance of
 *     <code>org.apache.commons.logging.impl.Log4JLogger</code>.</li>
 * <li>If JDK 1.4 or later is available, return an instance of
 *     <code>org.apache.commons.logging.impl.Jdk14Logger</code>.</li>
 * <li>Otherwise, return an instance of
 *     <code>org.apache.commons.logging.impl.NoOpLog</code>.</li>
 * </ul>
 * <p>
 * You can change the default behavior in one of two ways:
 * <ul>
 * <li>On the startup command line, set the system property
 *     <code>org.apache.commons.logging.log</code> to the name of the
 *     <code>org.apache.commons.logging.Log</code> implementation class
 *     you want to use.</li>
 * <li>At runtime, call <code>LogSource.setLogImplementation()</code>.</li>
 * </ul>
 *
 * @deprecated Use {@link LogFactory} instead - The default factory
 *  implementation performs exactly the same algorithm as this class did
 *
 * @version $Id: LogSource.java 1432675 2013-01-13 17:53:30Z tn $
 */
public class LogSource {

    // ------------------------------------------------------- Class Attributes

    static protected Hashtable logs = new Hashtable();

    /** Is log4j available (in the current classpath) */
    static protected boolean log4jIsAvailable = false;

    /** Is JDK 1.4 logging available */
    static protected boolean jdk14IsAvailable = false;

    /** Constructor for current log class */
    static protected Constructor logImplctor = null;

    // ----------------------------------------------------- Class Initializers

    static {

        // Is Log4J Available?
        try {
            log4jIsAvailable = null != Class.forName("org.apache.log4j.Logger");
        } catch (Throwable t) {
            log4jIsAvailable = false;
        }

        // Is JDK 1.4 Logging Available?
        try {
            jdk14IsAvailable = null != Class.forName("java.util.logging.Logger") &&
                               null != Class.forName("org.apache.commons.logging.impl.Jdk14Logger");
        } catch (Throwable t) {
            jdk14IsAvailable = false;
        }

        // Set the default Log implementation
        String name = null;
        try {
            name = System.getProperty("org.apache.commons.logging.log");
            if (name == null) {
                name = System.getProperty("org.apache.commons.logging.Log");
            }
        } catch (Throwable t) {
        }
        if (name != null) {
            try {
                setLogImplementation(name);
            } catch (Throwable t) {
                try {
                    setLogImplementation("org.apache.commons.logging.impl.NoOpLog");
                } catch (Throwable u) {
                    // ignored
                }
            }
        } else {
            try {
                if (log4jIsAvailable) {
                    setLogImplementation("org.apache.commons.logging.impl.Log4JLogger");
                } else if (jdk14IsAvailable) {
                    setLogImplementation("org.apache.commons.logging.impl.Jdk14Logger");
                } else {
                    setLogImplementation("org.apache.commons.logging.impl.NoOpLog");
                }
            } catch (Throwable t) {
                try {
                    setLogImplementation("org.apache.commons.logging.impl.NoOpLog");
                } catch (Throwable u) {
                    // ignored
                }
            }
        }

    }

    // ------------------------------------------------------------ Constructor

    /** Don't allow others to create instances. */
    private LogSource() {
    }

    // ---------------------------------------------------------- Class Methods

    /**
     * Set the log implementation/log implementation factory
     * by the name of the class.  The given class must implement {@link Log},
     * and provide a constructor that takes a single {@link String} argument
     * (containing the name of the log).
     */
    static public void setLogImplementation(String classname)
        throws LinkageError, NoSuchMethodException, SecurityException, ClassNotFoundException {
        try {
            Class logclass = Class.forName(classname);
            Class[] argtypes = new Class[1];
            argtypes[0] = "".getClass();
            logImplctor = logclass.getConstructor(argtypes);
        } catch (Throwable t) {
            logImplctor = null;
        }
    }

    /**
     * Set the log implementation/log implementation factory by class.
     * The given class must implement {@link Log}, and provide a constructor
     * that takes a single {@link String} argument (containing the name of the log).
     */
    static public void setLogImplementation(Class logclass)
        throws LinkageError, ExceptionInInitializerError, NoSuchMethodException, SecurityException {
        Class[] argtypes = new Class[1];
        argtypes[0] = "".getClass();
        logImplctor = logclass.getConstructor(argtypes);
    }

    /** Get a <code>Log</code> instance by class name. */
    static public Log getInstance(String name) {
        Log log = (Log) logs.get(name);
        if (null == log) {
            log = makeNewLogInstance(name);
            logs.put(name, log);
        }
        return log;
    }

    /** Get a <code>Log</code> instance by class. */
    static public Log getInstance(Class clazz) {
        return getInstance(clazz.getName());
    }

    /**
     * Create a new {@link Log} implementation, based on the given <i>name</i>.
     * <p>
     * The specific {@link Log} implementation returned is determined by the
     * value of the <tt>org.apache.commons.logging.log</tt> property. The value
     * of <tt>org.apache.commons.logging.log</tt> may be set to the fully specified
     * name of a class that implements the {@link Log} interface. This class must
     * also have a public constructor that takes a single {@link String} argument
     * (containing the <i>name</i> of the {@link Log} to be constructed.
     * <p>
     * When <tt>org.apache.commons.logging.log</tt> is not set, or when no corresponding
     * class can be found, this method will return a Log4JLogger if the log4j Logger
     * class is available in the {@link LogSource}'s classpath, or a Jdk14Logger if we
     * are on a JDK 1.4 or later system, or NoOpLog if neither of the above conditions is true.
     *
     * @param name the log name (or category)
     */
    static public Log makeNewLogInstance(String name) {
        Log log;
        try {
            Object[] args = { name };
            log = (Log) logImplctor.newInstance(args);
        } catch (Throwable t) {
            log = null;
        }
        if (null == log) {
            log = new NoOpLog(name);
        }
        return log;
    }

    /**
     * Returns a {@link String} array containing the names of
     * all logs known to me.
     */
    static public String[] getLogNames() {
        return (String[]) logs.keySet().toArray(new String[logs.size()]);
    }
}
