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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Factory for creating {@link Log} instances, with discovery and
 * configuration features similar to that employed by standard Java APIs
 * such as JAXP.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - This implementation is heavily
 * based on the SAXParserFactory and DocumentBuilderFactory implementations
 * (corresponding to the JAXP pluggability APIs) found in Apache Xerces.
 *
 * @version $Id: LogFactory.java 1606041 2014-06-27 11:56:59Z tn $
 */
public abstract class LogFactory {
    // Implementation note re AccessController usage
    //
    // It is important to keep code invoked via an AccessController to small
    // auditable blocks. Such code must carefully evaluate all user input
    // (parameters, system properties, config file contents, etc). As an
    // example, a Log implementation should not write to its logfile
    // with an AccessController anywhere in the call stack, otherwise an
    // insecure application could configure the log implementation to write
    // to a protected file using the privileges granted to JCL rather than
    // to the calling application.
    //
    // Under no circumstance should a non-private method return data that is
    // retrieved via an AccessController. That would allow an insecure app
    // to invoke that method and obtain data that it is not permitted to have.
    //
    // Invoking user-supplied code with an AccessController set is not a major
    // issue (eg invoking the constructor of the class specified by
    // HASHTABLE_IMPLEMENTATION_PROPERTY). That class will be in a different
    // trust domain, and therefore must have permissions to do whatever it
    // is trying to do regardless of the permissions granted to JCL. There is
    // a slight issue in that untrusted code may point that environment var
    // to another trusted library, in which case the code runs if both that
    // lib and JCL have the necessary permissions even when the untrusted
    // caller does not. That's a pretty hard route to exploit though.

    // ----------------------------------------------------- Manifest Constants

    /**
     * The name (<code>priority</code>) of the key in the config file used to
     * specify the priority of that particular config file. The associated value
     * is a floating-point number; higher values take priority over lower values.
     */
    public static final String PRIORITY_KEY = "priority";

    /**
     * The name (<code>use_tccl</code>) of the key in the config file used
     * to specify whether logging classes should be loaded via the thread
     * context class loader (TCCL), or not. By default, the TCCL is used.
     */
    public static final String TCCL_KEY = "use_tccl";

    /**
     * The name (<code>org.apache.commons.logging.LogFactory</code>) of the property
     * used to identify the LogFactory implementation
     * class name. This can be used as a system property, or as an entry in a
     * configuration properties file.
     */
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";

    /**
     * The fully qualified class name of the fallback <code>LogFactory</code>
     * implementation class to use, if no other can be found.
     */
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.LogFactoryImpl";

    /**
     * The name (<code>commons-logging.properties</code>) of the properties file to search for.
     */
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";

    /**
     * JDK1.3+ <a href="http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider">
     * 'Service Provider' specification</a>.
     */
    protected static final String SERVICE_ID =
        "META-INF/services/org.apache.commons.logging.LogFactory";

    /**
     * The name (<code>org.apache.commons.logging.diagnostics.dest</code>)
     * of the property used to enable internal commons-logging
     * diagnostic output, in order to get information on what logging
     * implementations are being discovered, what classloaders they
     * are loaded through, etc.
     * <p>
     * If a system property of this name is set then the value is
     * assumed to be the name of a file. The special strings
     * STDOUT or STDERR (case-sensitive) indicate output to
     * System.out and System.err respectively.
     * <p>
     * Diagnostic logging should be used only to debug problematic
     * configurations and should not be set in normal production use.
     */
    public static final String DIAGNOSTICS_DEST_PROPERTY =
        "org.apache.commons.logging.diagnostics.dest";

    /**
     * When null (the usual case), no diagnostic output will be
     * generated by LogFactory or LogFactoryImpl. When non-null,
     * interesting events will be written to the specified object.
     */
    private static PrintStream diagnosticsStream = null;

    /**
     * A string that gets prefixed to every message output by the
     * logDiagnostic method, so that users can clearly see which
     * LogFactory class is generating the output.
     */
    private static final String diagnosticPrefix;

    /**
     * Setting this system property
     * (<code>org.apache.commons.logging.LogFactory.HashtableImpl</code>)
     * value allows the <code>Hashtable</code> used to store
     * classloaders to be substituted by an alternative implementation.
     * <p>
     * <strong>Note:</strong> <code>LogFactory</code> will print:
     * <pre>
     * [ERROR] LogFactory: Load of custom hashtable failed
     * </pre>
     * to system error and then continue using a standard Hashtable.
     * <p>
     * <strong>Usage:</strong> Set this property when Java is invoked
     * and <code>LogFactory</code> will attempt to load a new instance
     * of the given implementation class.
     * For example, running the following ant scriplet:
     * <pre>
     *  &lt;java classname="${test.runner}" fork="yes" failonerror="${test.failonerror}"&gt;
     *     ...
     *     &lt;sysproperty
     *        key="org.apache.commons.logging.LogFactory.HashtableImpl"
     *        value="org.apache.commons.logging.AltHashtable"/&gt;
     *  &lt;/java&gt;
     * </pre>
     * will mean that <code>LogFactory</code> will load an instance of
     * <code>org.apache.commons.logging.AltHashtable</code>.
     * <p>
     * A typical use case is to allow a custom
     * Hashtable implementation using weak references to be substituted.
     * This will allow classloaders to be garbage collected without
     * the need to release them (on 1.3+ JVMs only, of course ;).
     */
    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY =
        "org.apache.commons.logging.LogFactory.HashtableImpl";

    /** Name used to load the weak hashtable implementation by names. */
    private static final String WEAK_HASHTABLE_CLASSNAME =
        "org.apache.commons.logging.impl.WeakHashtable";

    /**
     * A reference to the classloader that loaded this class. This is the
     * same as LogFactory.class.getClassLoader(). However computing this
     * value isn't quite as simple as that, as we potentially need to use
     * AccessControllers etc. It's more efficient to compute it once and
     * cache it here.
     */
    private static final ClassLoader thisClassLoader;

    // ----------------------------------------------------------- Constructors

    /**
     * Protected constructor that is not available for public use.
     */
    protected LogFactory() {
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     */
    public abstract Object getAttribute(String name);

    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  If there are no such attributes, a zero
     * length array is returned.
     */
    public abstract String[] getAttributeNames();

    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public abstract Log getInstance(Class clazz)
        throws LogConfigurationException;

    /**
     * Construct (if necessary) and return a <code>Log</code> instance,
     * using the factory's current set of configuration attributes.
     * <p>
     * <strong>NOTE</strong> - Depending upon the implementation of
     * the <code>LogFactory</code> you are using, the <code>Log</code>
     * instance you are returned may or may not be local to the current
     * application, and may or may not be returned again on a subsequent
     * call with the same name argument.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public abstract Log getInstance(String name)
        throws LogConfigurationException;

    /**
     * Release any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public abstract void release();

    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public abstract void removeAttribute(String name);

    /**
     * Set the configuration attribute with the specified name.  Calling
     * this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *  to remove any setting for this attribute
     */
    public abstract void setAttribute(String name, Object value);

    // ------------------------------------------------------- Static Variables

    /**
     * The previously constructed <code>LogFactory</code> instances, keyed by
     * the <code>ClassLoader</code> with which it was created.
     */
    protected static Hashtable factories = null;

    /**
     * Previously constructed <code>LogFactory</code> instance as in the
     * <code>factories</code> map, but for the case where
     * <code>getClassLoader</code> returns <code>null</code>.
     * This can happen when:
     * <ul>
     * <li>using JDK1.1 and the calling code is loaded via the system
     *  classloader (very common)</li>
     * <li>using JDK1.2+ and the calling code is loaded via the boot
     *  classloader (only likely for embedded systems work).</li>
     * </ul>
     * Note that <code>factories</code> is a <i>Hashtable</i> (not a HashMap),
     * and hashtables don't allow null as a key.
     * @deprecated since 1.1.2
     */
    protected static volatile LogFactory nullClassLoaderFactory = null;

    /**
     * Create the hashtable which will be used to store a map of
     * (context-classloader -> logfactory-object). Version 1.2+ of Java
     * supports "weak references", allowing a custom Hashtable class
     * to be used which uses only weak references to its keys. Using weak
     * references can fix memory leaks on webapp unload in some cases (though
     * not all). Version 1.1 of Java does not support weak references, so we
     * must dynamically determine which we are using. And just for fun, this
     * code also supports the ability for a system property to specify an
     * arbitrary Hashtable implementation name.
     * <p>
     * Note that the correct way to ensure no memory leaks occur is to ensure
     * that LogFactory.release(contextClassLoader) is called whenever a
     * webapp is undeployed.
     */
    private static final Hashtable createFactoryStore() {
        Hashtable result = null;
        String storeImplementationClass;
        try {
            storeImplementationClass = getSystemProperty(HASHTABLE_IMPLEMENTATION_PROPERTY, null);
        } catch (SecurityException ex) {
            // Permissions don't allow this to be accessed. Default to the "modern"
            // weak hashtable implementation if it is available.
            storeImplementationClass = null;
        }

        if (storeImplementationClass == null) {
            storeImplementationClass = WEAK_HASHTABLE_CLASSNAME;
        }
        try {
            Class implementationClass = Class.forName(storeImplementationClass);
            result = (Hashtable) implementationClass.newInstance();
        } catch (Throwable t) {
            handleThrowable(t); // may re-throw t

            // ignore
            if (!WEAK_HASHTABLE_CLASSNAME.equals(storeImplementationClass)) {
                // if the user's trying to set up a custom implementation, give a clue
                if (isDiagnosticsEnabled()) {
                    // use internal logging to issue the warning
                    logDiagnostic("[ERROR] LogFactory: Load of custom hashtable failed");
                } else {
                    // we *really* want this output, even if diagnostics weren't
                    // explicitly enabled by the user.
                    System.err.println("[ERROR] LogFactory: Load of custom hashtable failed");
                }
            }
        }
        if (result == null) {
            result = new Hashtable();
        }
        return result;
    }

    // --------------------------------------------------------- Static Methods

    /** Utility method to safely trim a string. */
    private static String trim(String src) {
        if (src == null) {
            return null;
        }
        return src.trim();
    }

    /**
     * Checks whether the supplied Throwable is one that needs to be
     * re-thrown and ignores all others.
     *
     * The following errors are re-thrown:
     * <ul>
     *   <li>ThreadDeath</li>
     *   <li>VirtualMachineError</li>
     * </ul>
     *
     * @param t the Throwable to check
     */
    protected static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently ignored
    }

    /**
     * Construct (if necessary) and return a <code>LogFactory</code>
     * instance, using the following ordered lookup procedure to determine
     * the name of the implementation class to be loaded.
     * <p>
     * <ul>
     * <li>The <code>org.apache.commons.logging.LogFactory</code> system
     *     property.</li>
     * <li>The JDK 1.3 Service Discovery mechanism</li>
     * <li>Use the properties file <code>commons-logging.properties</code>
     *     file, if found in the class path of this class.  The configuration
     *     file is in standard <code>java.util.Properties</code> format and
     *     contains the fully qualified name of the implementation class
     *     with the key being the system property defined above.</li>
     * <li>Fall back to a default implementation class
     *     (<code>org.apache.commons.logging.impl.LogFactoryImpl</code>).</li>
     * </ul>
     * <p>
     * <em>NOTE</em> - If the properties file method of identifying the
     * <code>LogFactory</code> implementation class is utilized, all of the
     * properties defined in this file will be set as configuration attributes
     * on the corresponding <code>LogFactory</code> instance.
     * <p>
     * <em>NOTE</em> - In a multi-threaded environment it is possible
     * that two different instances will be returned for the same
     * classloader environment.
     *
     * @throws LogConfigurationException if the implementation class is not
     *  available or cannot be instantiated.
     */
    public static LogFactory getFactory() throws LogConfigurationException {
        // Identify the class loader we will be using
        ClassLoader contextClassLoader = getContextClassLoaderInternal();

        if (contextClassLoader == null) {
            // This is an odd enough situation to report about. This
            // output will be a nuisance on JDK1.1, as the system
            // classloader is null in that environment.
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Context classloader is null.");
            }
        }

        // Return any previously registered factory for this class loader
        LogFactory factory = getCachedFactory(contextClassLoader);
        if (factory != null) {
            return factory;
        }

        if (isDiagnosticsEnabled()) {
            logDiagnostic(
                    "[LOOKUP] LogFactory implementation requested for the first time for context classloader " +
                    objectId(contextClassLoader));
            logHierarchy("[LOOKUP] ", contextClassLoader);
        }

        // Load properties file.
        //
        // If the properties file exists, then its contents are used as
        // "attributes" on the LogFactory implementation class. One particular
        // property may also control which LogFactory concrete subclass is
        // used, but only if other discovery mechanisms fail..
        //
        // As the properties file (if it exists) will be used one way or
        // another in the end we may as well look for it first.

        Properties props = getConfigurationFile(contextClassLoader, FACTORY_PROPERTIES);

        // Determine whether we will be using the thread context class loader to
        // load logging classes or not by checking the loaded properties file (if any).
        ClassLoader baseClassLoader = contextClassLoader;
        if (props != null) {
            String useTCCLStr = props.getProperty(TCCL_KEY);
            if (useTCCLStr != null) {
                // The Boolean.valueOf(useTCCLStr).booleanValue() formulation
                // is required for Java 1.2 compatibility.
                if (Boolean.valueOf(useTCCLStr).booleanValue() == false) {
                    // Don't use current context classloader when locating any
                    // LogFactory or Log classes, just use the class that loaded
                    // this abstract class. When this class is deployed in a shared
                    // classpath of a container, it means webapps cannot deploy their
                    // own logging implementations. It also means that it is up to the
                    // implementation whether to load library-specific config files
                    // from the TCCL or not.
                    baseClassLoader = thisClassLoader;
                }
            }
        }

        // Determine which concrete LogFactory subclass to use.
        // First, try a global system property
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[LOOKUP] Looking for system property [" + FACTORY_PROPERTY +
                          "] to define the LogFactory subclass to use...");
        }

        try {
            String factoryClass = getSystemProperty(FACTORY_PROPERTY, null);
            if (factoryClass != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] Creating an instance of LogFactory class '" + factoryClass +
                                  "' as specified by system property " + FACTORY_PROPERTY);
                }
                factory = newFactory(factoryClass, baseClassLoader, contextClassLoader);
            } else {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] No system property [" + FACTORY_PROPERTY + "] defined.");
                }
            }
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] A security exception occurred while trying to create an" +
                              " instance of the custom factory class" + ": [" + trim(e.getMessage()) +
                              "]. Trying alternative implementations...");
            }
            // ignore
        } catch (RuntimeException e) {
            // This is not consistent with the behaviour when a bad LogFactory class is
            // specified in a services file.
            //
            // One possible exception that can occur here is a ClassCastException when
            // the specified class wasn't castable to this LogFactory type.
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] An exception occurred while trying to create an" +
                              " instance of the custom factory class" + ": [" +
                              trim(e.getMessage()) +
                              "] as specified by a system property.");
            }
            throw e;
        }

        // Second, try to find a service by using the JDK1.3 class
        // discovery mechanism, which involves putting a file with the name
        // of an interface class in the META-INF/services directory, where the
        // contents of the file is a single line specifying a concrete class
        // that implements the desired interface.

        if (factory == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] Looking for a resource file of name [" + SERVICE_ID +
                              "] to define the LogFactory subclass to use...");
            }
            try {
                final InputStream is = getResourceAsStream(contextClassLoader, SERVICE_ID);

                if( is != null ) {
                    // This code is needed by EBCDIC and other strange systems.
                    // It's a fix for bugs reported in xerces
                    BufferedReader rd;
                    try {
                        rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    } catch (java.io.UnsupportedEncodingException e) {
                        rd = new BufferedReader(new InputStreamReader(is));
                    }

                    String factoryClassName = rd.readLine();
                    rd.close();

                    if (factoryClassName != null && ! "".equals(factoryClassName)) {
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("[LOOKUP]  Creating an instance of LogFactory class " +
                                          factoryClassName +
                                          " as specified by file '" + SERVICE_ID +
                                          "' which was present in the path of the context classloader.");
                        }
                        factory = newFactory(factoryClassName, baseClassLoader, contextClassLoader );
                    }
                } else {
                    // is == null
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic("[LOOKUP] No resource file with name '" + SERVICE_ID + "' found.");
                    }
                }
            } catch (Exception ex) {
                // note: if the specified LogFactory class wasn't compatible with LogFactory
                // for some reason, a ClassCastException will be caught here, and attempts will
                // continue to find a compatible class.
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(
                        "[LOOKUP] A security exception occurred while trying to create an" +
                        " instance of the custom factory class" +
                        ": [" + trim(ex.getMessage()) +
                        "]. Trying alternative implementations...");
                }
                // ignore
            }
        }

        // Third try looking into the properties file read earlier (if found)

        if (factory == null) {
            if (props != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(
                        "[LOOKUP] Looking in properties file for entry with key '" + FACTORY_PROPERTY +
                        "' to define the LogFactory subclass to use...");
                }
                String factoryClass = props.getProperty(FACTORY_PROPERTY);
                if (factoryClass != null) {
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic(
                            "[LOOKUP] Properties file specifies LogFactory subclass '" + factoryClass + "'");
                    }
                    factory = newFactory(factoryClass, baseClassLoader, contextClassLoader);

                    // TODO: think about whether we need to handle exceptions from newFactory
                } else {
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic("[LOOKUP] Properties file has no entry specifying LogFactory subclass.");
                    }
                }
            } else {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] No properties file available to determine" + " LogFactory subclass from..");
                }
            }
        }

        // Fourth, try the fallback implementation class

        if (factory == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(
                    "[LOOKUP] Loading the default LogFactory implementation '" + FACTORY_DEFAULT +
                    "' via the same classloader that loaded this LogFactory" +
                    " class (ie not looking in the context classloader).");
            }

            // Note: unlike the above code which can try to load custom LogFactory
            // implementations via the TCCL, we don't try to load the default LogFactory
            // implementation via the context classloader because:
            // * that can cause problems (see comments in newFactory method)
            // * no-one should be customising the code of the default class
            // Yes, we do give up the ability for the child to ship a newer
            // version of the LogFactoryImpl class and have it used dynamically
            // by an old LogFactory class in the parent, but that isn't
            // necessarily a good idea anyway.
            factory = newFactory(FACTORY_DEFAULT, thisClassLoader, contextClassLoader);
        }

        if (factory != null) {
            /**
             * Always cache using context class loader.
             */
            cacheFactory(contextClassLoader, factory);

            if (props != null) {
                Enumeration names = props.propertyNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    String value = props.getProperty(name);
                    factory.setAttribute(name, value);
                }
            }
        }

        return factory;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class from which a log name will be derived
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(Class clazz) throws LogConfigurationException {
        return getFactory().getInstance(clazz);
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(String name) throws LogConfigurationException {
        return getFactory().getInstance(name);
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances that have been associated with the specified class loader
     * (if any), after calling the instance method <code>release()</code> on
     * each of them.
     *
     * @param classLoader ClassLoader for which to release the LogFactory
     */
    public static void release(ClassLoader classLoader) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Releasing factory for classloader " + objectId(classLoader));
        }
        // factories is not final and could be replaced in this block.
        final Hashtable factories = LogFactory.factories;
        synchronized (factories) {
            if (classLoader == null) {
                if (nullClassLoaderFactory != null) {
                    nullClassLoaderFactory.release();
                    nullClassLoaderFactory = null;
                }
            } else {
                final LogFactory factory = (LogFactory) factories.get(classLoader);
                if (factory != null) {
                    factory.release();
                    factories.remove(classLoader);
                }
            }
        }
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method <code>release()</code> on
     * each of them.  This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static void releaseAll() {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Releasing factory for all classloaders.");
        }
        // factories is not final and could be replaced in this block.
        final Hashtable factories = LogFactory.factories;
        synchronized (factories) {
            final Enumeration elements = factories.elements();
            while (elements.hasMoreElements()) {
                LogFactory element = (LogFactory) elements.nextElement();
                element.release();
            }
            factories.clear();

            if (nullClassLoaderFactory != null) {
                nullClassLoaderFactory.release();
                nullClassLoaderFactory = null;
            }
        }
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Safely get access to the classloader for the specified class.
     * <p>
     * Theoretically, calling getClassLoader can throw a security exception,
     * and so should be done under an AccessController in order to provide
     * maximum flexibility. However in practice people don't appear to use
     * security policies that forbid getClassLoader calls. So for the moment
     * all code is written to call this method rather than Class.getClassLoader,
     * so that we could put AccessController stuff in this method without any
     * disruption later if we need to.
     * <p>
     * Even when using an AccessController, however, this method can still
     * throw SecurityException. Commons-logging basically relies on the
     * ability to access classloaders, ie a policy that forbids all
     * classloader access will also prevent commons-logging from working:
     * currently this method will throw an exception preventing the entire app
     * from starting up. Maybe it would be good to detect this situation and
     * just disable all commons-logging? Not high priority though - as stated
     * above, security policies that prevent classloader access aren't common.
     * <p>
     * Note that returning an object fetched via an AccessController would
     * technically be a security flaw anyway; untrusted code that has access
     * to a trusted JCL library could use it to fetch the classloader for
     * a class even when forbidden to do so directly.
     *
     * @since 1.1
     */
    protected static ClassLoader getClassLoader(Class clazz) {
        try {
            return clazz.getClassLoader();
        } catch (SecurityException ex) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Unable to get classloader for class '" + clazz +
                              "' due to security restrictions - " + ex.getMessage());
            }
            throw ex;
        }
    }

    /**
     * Returns the current context classloader.
     * <p>
     * In versions prior to 1.1, this method did not use an AccessController.
     * In version 1.1, an AccessController wrapper was incorrectly added to
     * this method, causing a minor security flaw.
     * <p>
     * In version 1.1.1 this change was reverted; this method no longer uses
     * an AccessController. User code wishing to obtain the context classloader
     * must invoke this method via AccessController.doPrivileged if it needs
     * support for that.
     *
     * @return the context classloader associated with the current thread,
     *  or null if security doesn't allow it.
     * @throws LogConfigurationException if there was some weird error while
     *  attempting to get the context classloader.
     */
    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return directGetContextClassLoader();
    }

    /**
     * Calls LogFactory.directGetContextClassLoader under the control of an
     * AccessController class. This means that java code running under a
     * security manager that forbids access to ClassLoaders will still work
     * if this class is given appropriate privileges, even when the caller
     * doesn't have such privileges. Without using an AccessController, the
     * the entire call stack must have the privilege before the call is
     * allowed.
     *
     * @return the context classloader associated with the current thread,
     *  or null if security doesn't allow it.
     * @throws LogConfigurationException if there was some weird error while
     *  attempting to get the context classloader.
     */
    private static ClassLoader getContextClassLoaderInternal() throws LogConfigurationException {
        return (ClassLoader)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return directGetContextClassLoader();
                }
            });
    }

    /**
     * Return the thread context class loader if available; otherwise return null.
     * <p>
     * Most/all code should call getContextClassLoaderInternal rather than
     * calling this method directly.
     * <p>
     * The thread context class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * <p>
     * Note that no internal logging is done within this method because
     * this method is called every time LogFactory.getLogger() is called,
     * and we don't want too much output generated here.
     *
     * @throws LogConfigurationException if a suitable class loader
     *  cannot be identified.
     * @return the thread's context classloader or {@code null} if the java security
     *  policy forbids access to the context classloader from one of the classes
     *  in the current call stack.
     * @since 1.1
     */
    protected static ClassLoader directGetContextClassLoader() throws LogConfigurationException {
        ClassLoader classLoader = null;

        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException ex) {
            /**
             * getContextClassLoader() throws SecurityException when
             * the context class loader isn't an ancestor of the
             * calling class's class loader, or if security
             * permissions are restricted.
             *
             * We ignore this exception to be consistent with the previous
             * behavior (e.g. 1.1.3 and earlier).
             */
            // ignore
        }

        // Return the selected class loader
        return classLoader;
    }

    /**
     * Check cached factories (keyed by contextClassLoader)
     *
     * @param contextClassLoader is the context classloader associated
     * with the current thread. This allows separate LogFactory objects
     * per component within a container, provided each component has
     * a distinct context classloader set. This parameter may be null
     * in JDK1.1, and in embedded systems where jcl-using code is
     * placed in the bootclasspath.
     *
     * @return the factory associated with the specified classloader if
     *  one has previously been created, or null if this is the first time
     *  we have seen this particular classloader.
     */
    private static LogFactory getCachedFactory(ClassLoader contextClassLoader) {
        if (contextClassLoader == null) {
            // We have to handle this specially, as factories is a Hashtable
            // and those don't accept null as a key value.
            //
            // nb: nullClassLoaderFactory might be null. That's ok.
            return nullClassLoaderFactory;
        } else {
            return (LogFactory) factories.get(contextClassLoader);
        }
    }

    /**
     * Remember this factory, so later calls to LogFactory.getCachedFactory
     * can return the previously created object (together with all its
     * cached Log objects).
     *
     * @param classLoader should be the current context classloader. Note that
     *  this can be null under some circumstances; this is ok.
     * @param factory should be the factory to cache. This should never be null.
     */
    private static void cacheFactory(ClassLoader classLoader, LogFactory factory) {
        // Ideally we would assert(factory != null) here. However reporting
        // errors from within a logging implementation is a little tricky!

        if (factory != null) {
            if (classLoader == null) {
                nullClassLoaderFactory = factory;
            } else {
                factories.put(classLoader, factory);
            }
        }
    }

    /**
     * Return a new instance of the specified <code>LogFactory</code>
     * implementation class, loaded by the specified class loader.
     * If that fails, try the class loader used to load this
     * (abstract) LogFactory.
     * <h2>ClassLoader conflicts</h2>
     * <p>
     * Note that there can be problems if the specified ClassLoader is not the
     * same as the classloader that loaded this class, ie when loading a
     * concrete LogFactory subclass via a context classloader.
     * <p>
     * The problem is the same one that can occur when loading a concrete Log
     * subclass via a context classloader.
     * <p>
     * The problem occurs when code running in the context classloader calls
     * class X which was loaded via a parent classloader, and class X then calls
     * LogFactory.getFactory (either directly or via LogFactory.getLog). Because
     * class X was loaded via the parent, it binds to LogFactory loaded via
     * the parent. When the code in this method finds some LogFactoryYYYY
     * class in the child (context) classloader, and there also happens to be a
     * LogFactory class defined in the child classloader, then LogFactoryYYYY
     * will be bound to LogFactory@childloader. It cannot be cast to
     * LogFactory@parentloader, ie this method cannot return the object as
     * the desired type. Note that it doesn't matter if the LogFactory class
     * in the child classloader is identical to the LogFactory class in the
     * parent classloader, they are not compatible.
     * <p>
     * The solution taken here is to simply print out an error message when
     * this occurs then throw an exception. The deployer of the application
     * must ensure they remove all occurrences of the LogFactory class from
     * the child classloader in order to resolve the issue. Note that they
     * do not have to move the custom LogFactory subclass; that is ok as
     * long as the only LogFactory class it can find to bind to is in the
     * parent classloader.
     *
     * @param factoryClass Fully qualified name of the <code>LogFactory</code>
     *  implementation class
     * @param classLoader ClassLoader from which to load this class
     * @param contextClassLoader is the context that this new factory will
     *  manage logging for.
     * @throws LogConfigurationException if a suitable instance
     *  cannot be created
     * @since 1.1
     */
    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader,
                                           final ClassLoader contextClassLoader)
        throws LogConfigurationException {
        // Note that any unchecked exceptions thrown by the createFactory
        // method will propagate out of this method; in particular a
        // ClassCastException can be thrown.
        Object result = AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return createFactory(factoryClass, classLoader);
                }
            });

        if (result instanceof LogConfigurationException) {
            LogConfigurationException ex = (LogConfigurationException) result;
            if (isDiagnosticsEnabled()) {
                logDiagnostic("An error occurred while loading the factory class:" + ex.getMessage());
            }
            throw ex;
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Created object " + objectId(result) + " to manage classloader " +
                          objectId(contextClassLoader));
        }
        return (LogFactory)result;
    }

    /**
     * Method provided for backwards compatibility; see newFactory version that
     * takes 3 parameters.
     * <p>
     * This method would only ever be called in some rather odd situation.
     * Note that this method is static, so overriding in a subclass doesn't
     * have any effect unless this method is called from a method in that
     * subclass. However this method only makes sense to use from the
     * getFactory method, and as that is almost always invoked via
     * LogFactory.getFactory, any custom definition in a subclass would be
     * pointless. Only a class with a custom getFactory method, then invoked
     * directly via CustomFactoryImpl.getFactory or similar would ever call
     * this. Anyway, it's here just in case, though the "managed class loader"
     * value output to the diagnostics will not report the correct value.
     */
    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader) {
        return newFactory(factoryClass, classLoader, null);
    }

    /**
     * Implements the operations described in the javadoc for newFactory.
     *
     * @param factoryClass
     * @param classLoader used to load the specified factory class. This is
     *  expected to be either the TCCL or the classloader which loaded this
     *  class. Note that the classloader which loaded this class might be
     *  "null" (ie the bootloader) for embedded systems.
     * @return either a LogFactory object or a LogConfigurationException object.
     * @since 1.1
     */
    protected static Object createFactory(String factoryClass, ClassLoader classLoader) {
        // This will be used to diagnose bad configurations
        // and allow a useful message to be sent to the user
        Class logFactoryClass = null;
        try {
            if (classLoader != null) {
                try {
                    // First the given class loader param (thread class loader)

                    // Warning: must typecast here & allow exception
                    // to be generated/caught & recast properly.
                    logFactoryClass = classLoader.loadClass(factoryClass);
                    if (LogFactory.class.isAssignableFrom(logFactoryClass)) {
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Loaded class " + logFactoryClass.getName() +
                                          " from classloader " + objectId(classLoader));
                        }
                    } else {
                        //
                        // This indicates a problem with the ClassLoader tree.
                        // An incompatible ClassLoader was used to load the
                        // implementation.
                        // As the same classes
                        // must be available in multiple class loaders,
                        // it is very likely that multiple JCL jars are present.
                        // The most likely fix for this
                        // problem is to remove the extra JCL jars from the
                        // ClassLoader hierarchy.
                        //
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Factory class " + logFactoryClass.getName() +
                                          " loaded from classloader " + objectId(logFactoryClass.getClassLoader()) +
                                          " does not extend '" + LogFactory.class.getName() +
                                          "' as loaded by this classloader.");
                            logHierarchy("[BAD CL TREE] ", classLoader);
                        }
                    }

                    return (LogFactory) logFactoryClass.newInstance();

                } catch (ClassNotFoundException ex) {
                    if (classLoader == thisClassLoader) {
                        // Nothing more to try, onwards.
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Unable to locate any class called '" + factoryClass +
                                          "' via classloader " + objectId(classLoader));
                        }
                        throw ex;
                    }
                    // ignore exception, continue
                } catch (NoClassDefFoundError e) {
                    if (classLoader == thisClassLoader) {
                        // Nothing more to try, onwards.
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Class '" + factoryClass + "' cannot be loaded" +
                                          " via classloader " + objectId(classLoader) +
                                          " - it depends on some other class that cannot be found.");
                        }
                        throw e;
                    }
                    // ignore exception, continue
                } catch (ClassCastException e) {
                    if (classLoader == thisClassLoader) {
                        // There's no point in falling through to the code below that
                        // tries again with thisClassLoader, because we've just tried
                        // loading with that loader (not the TCCL). Just throw an
                        // appropriate exception here.

                        final boolean implementsLogFactory = implementsLogFactory(logFactoryClass);

                        //
                        // Construct a good message: users may not actual expect that a custom implementation
                        // has been specified. Several well known containers use this mechanism to adapt JCL
                        // to their native logging system.
                        //
                        final StringBuffer msg = new StringBuffer();
                        msg.append("The application has specified that a custom LogFactory implementation ");
                        msg.append("should be used but Class '");
                        msg.append(factoryClass);
                        msg.append("' cannot be converted to '");
                        msg.append(LogFactory.class.getName());
                        msg.append("'. ");
                        if (implementsLogFactory) {
                            msg.append("The conflict is caused by the presence of multiple LogFactory classes ");
                            msg.append("in incompatible classloaders. ");
                            msg.append("Background can be found in http://commons.apache.org/logging/tech.html. ");
                            msg.append("If you have not explicitly specified a custom LogFactory then it is likely ");
                            msg.append("that the container has set one without your knowledge. ");
                            msg.append("In this case, consider using the commons-logging-adapters.jar file or ");
                            msg.append("specifying the standard LogFactory from the command line. ");
                        } else {
                            msg.append("Please check the custom implementation. ");
                        }
                        msg.append("Help can be found @http://commons.apache.org/logging/troubleshooting.html.");

                        if (isDiagnosticsEnabled()) {
                            logDiagnostic(msg.toString());
                        }

                        throw new ClassCastException(msg.toString());
                    }

                    // Ignore exception, continue. Presumably the classloader was the
                    // TCCL; the code below will try to load the class via thisClassLoader.
                    // This will handle the case where the original calling class is in
                    // a shared classpath but the TCCL has a copy of LogFactory and the
                    // specified LogFactory implementation; we will fall back to using the
                    // LogFactory implementation from the same classloader as this class.
                    //
                    // Issue: this doesn't handle the reverse case, where this LogFactory
                    // is in the webapp, and the specified LogFactory implementation is
                    // in a shared classpath. In that case:
                    // (a) the class really does implement LogFactory (bad log msg above)
                    // (b) the fallback code will result in exactly the same problem.
                }
            }

            /* At this point, either classLoader == null, OR
             * classLoader was unable to load factoryClass.
             *
             * In either case, we call Class.forName, which is equivalent
             * to LogFactory.class.getClassLoader().load(name), ie we ignore
             * the classloader parameter the caller passed, and fall back
             * to trying the classloader associated with this class. See the
             * javadoc for the newFactory method for more info on the
             * consequences of this.
             *
             * Notes:
             * * LogFactory.class.getClassLoader() may return 'null'
             *   if LogFactory is loaded by the bootstrap classloader.
             */
            // Warning: must typecast here & allow exception
            // to be generated/caught & recast properly.
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Unable to load factory class via classloader " + objectId(classLoader) +
                              " - trying the classloader associated with this LogFactory.");
            }
            logFactoryClass = Class.forName(factoryClass);
            return (LogFactory) logFactoryClass.newInstance();
        } catch (Exception e) {
            // Check to see if we've got a bad configuration
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Unable to create LogFactory instance.");
            }
            if (logFactoryClass != null && !LogFactory.class.isAssignableFrom(logFactoryClass)) {
                return new LogConfigurationException(
                    "The chosen LogFactory implementation does not extend LogFactory." +
                    " Please check your configuration.", e);
            }
            return new LogConfigurationException(e);
        }
    }

    /**
     * Determines whether the given class actually implements <code>LogFactory</code>.
     * Diagnostic information is also logged.
     * <p>
     * <strong>Usage:</strong> to diagnose whether a classloader conflict is the cause
     * of incompatibility. The test used is whether the class is assignable from
     * the <code>LogFactory</code> class loaded by the class's classloader.
     * @param logFactoryClass <code>Class</code> which may implement <code>LogFactory</code>
     * @return true if the <code>logFactoryClass</code> does extend
     * <code>LogFactory</code> when that class is loaded via the same
     * classloader that loaded the <code>logFactoryClass</code>.
     */
    private static boolean implementsLogFactory(Class logFactoryClass) {
        boolean implementsLogFactory = false;
        if (logFactoryClass != null) {
            try {
                ClassLoader logFactoryClassLoader = logFactoryClass.getClassLoader();
                if (logFactoryClassLoader == null) {
                    logDiagnostic("[CUSTOM LOG FACTORY] was loaded by the boot classloader");
                } else {
                    logHierarchy("[CUSTOM LOG FACTORY] ", logFactoryClassLoader);
                    Class factoryFromCustomLoader
                        = Class.forName("org.apache.commons.logging.LogFactory", false, logFactoryClassLoader);
                    implementsLogFactory = factoryFromCustomLoader.isAssignableFrom(logFactoryClass);
                    if (implementsLogFactory) {
                        logDiagnostic("[CUSTOM LOG FACTORY] " + logFactoryClass.getName() +
                                      " implements LogFactory but was loaded by an incompatible classloader.");
                    } else {
                        logDiagnostic("[CUSTOM LOG FACTORY] " + logFactoryClass.getName() +
                                      " does not implement LogFactory.");
                    }
                }
            } catch (SecurityException e) {
                //
                // The application is running within a hostile security environment.
                // This will make it very hard to diagnose issues with JCL.
                // Consider running less securely whilst debugging this issue.
                //
                logDiagnostic("[CUSTOM LOG FACTORY] SecurityException thrown whilst trying to determine whether " +
                              "the compatibility was caused by a classloader conflict: " + e.getMessage());
            } catch (LinkageError e) {
                //
                // This should be an unusual circumstance.
                // LinkageError's usually indicate that a dependent class has incompatibly changed.
                // Another possibility may be an exception thrown by an initializer.
                // Time for a clean rebuild?
                //
                logDiagnostic("[CUSTOM LOG FACTORY] LinkageError thrown whilst trying to determine whether " +
                              "the compatibility was caused by a classloader conflict: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                //
                // LogFactory cannot be loaded by the classloader which loaded the custom factory implementation.
                // The custom implementation is not viable until this is corrected.
                // Ensure that the JCL jar and the custom class are available from the same classloader.
                // Running with diagnostics on should give information about the classloaders used
                // to load the custom factory.
                //
                logDiagnostic("[CUSTOM LOG FACTORY] LogFactory class cannot be loaded by classloader which loaded " +
                              "the custom LogFactory implementation. Is the custom factory in the right classloader?");
            }
        }
        return implementsLogFactory;
    }

    /**
     * Applets may run in an environment where accessing resources of a loader is
     * a secure operation, but where the commons-logging library has explicitly
     * been granted permission for that operation. In this case, we need to
     * run the operation using an AccessController.
     */
    private static InputStream getResourceAsStream(final ClassLoader loader, final String name) {
        return (InputStream)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    if (loader != null) {
                        return loader.getResourceAsStream(name);
                    } else {
                        return ClassLoader.getSystemResourceAsStream(name);
                    }
                }
            });
    }

    /**
     * Given a filename, return an enumeration of URLs pointing to
     * all the occurrences of that filename in the classpath.
     * <p>
     * This is just like ClassLoader.getResources except that the
     * operation is done under an AccessController so that this method will
     * succeed when this jarfile is privileged but the caller is not.
     * This method must therefore remain private to avoid security issues.
     * <p>
     * If no instances are found, an Enumeration is returned whose
     * hasMoreElements method returns false (ie an "empty" enumeration).
     * If resources could not be listed for some reason, null is returned.
     */
    private static Enumeration getResources(final ClassLoader loader, final String name) {
        PrivilegedAction action =
            new PrivilegedAction() {
                public Object run() {
                    try {
                        if (loader != null) {
                            return loader.getResources(name);
                        } else {
                            return ClassLoader.getSystemResources(name);
                        }
                    } catch (IOException e) {
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Exception while trying to find configuration file " +
                                          name + ":" + e.getMessage());
                        }
                        return null;
                    } catch (NoSuchMethodError e) {
                        // we must be running on a 1.1 JVM which doesn't support
                        // ClassLoader.getSystemResources; just return null in
                        // this case.
                        return null;
                    }
                }
            };
        Object result = AccessController.doPrivileged(action);
        return (Enumeration) result;
    }

    /**
     * Given a URL that refers to a .properties file, load that file.
     * This is done under an AccessController so that this method will
     * succeed when this jarfile is privileged but the caller is not.
     * This method must therefore remain private to avoid security issues.
     * <p>
     * {@code Null} is returned if the URL cannot be opened.
     */
    private static Properties getProperties(final URL url) {
        PrivilegedAction action =
            new PrivilegedAction() {
                public Object run() {
                    InputStream stream = null;
                    try {
                        // We must ensure that useCaches is set to false, as the
                        // default behaviour of java is to cache file handles, and
                        // this "locks" files, preventing hot-redeploy on windows.
                        URLConnection connection = url.openConnection();
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                        if (stream != null) {
                            Properties props = new Properties();
                            props.load(stream);
                            stream.close();
                            stream = null;
                            return props;
                        }
                    } catch (IOException e) {
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Unable to read URL " + url);
                        }
                    } finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                // ignore exception; this should not happen
                                if (isDiagnosticsEnabled()) {
                                    logDiagnostic("Unable to close stream for URL " + url);
                                }
                            }
                        }
                    }

                    return null;
                }
            };
        return (Properties) AccessController.doPrivileged(action);
    }

    /**
     * Locate a user-provided configuration file.
     * <p>
     * The classpath of the specified classLoader (usually the context classloader)
     * is searched for properties files of the specified name. If none is found,
     * null is returned. If more than one is found, then the file with the greatest
     * value for its PRIORITY property is returned. If multiple files have the
     * same PRIORITY value then the first in the classpath is returned.
     * <p>
     * This differs from the 1.0.x releases; those always use the first one found.
     * However as the priority is a new field, this change is backwards compatible.
     * <p>
     * The purpose of the priority field is to allow a webserver administrator to
     * override logging settings in all webapps by placing a commons-logging.properties
     * file in a shared classpath location with a priority > 0; this overrides any
     * commons-logging.properties files without priorities which are in the
     * webapps. Webapps can also use explicit priorities to override a configuration
     * file in the shared classpath if needed.
     */
    private static final Properties getConfigurationFile(ClassLoader classLoader, String fileName) {
        Properties props = null;
        double priority = 0.0;
        URL propsUrl = null;
        try {
            Enumeration urls = getResources(classLoader, fileName);

            if (urls == null) {
                return null;
            }

            while (urls.hasMoreElements()) {
                URL url = (URL) urls.nextElement();

                Properties newProps = getProperties(url);
                if (newProps != null) {
                    if (props == null) {
                        propsUrl = url;
                        props = newProps;
                        String priorityStr = props.getProperty(PRIORITY_KEY);
                        priority = 0.0;
                        if (priorityStr != null) {
                            priority = Double.parseDouble(priorityStr);
                        }

                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("[LOOKUP] Properties file found at '" + url + "'" +
                                          " with priority " + priority);
                        }
                    } else {
                        String newPriorityStr = newProps.getProperty(PRIORITY_KEY);
                        double newPriority = 0.0;
                        if (newPriorityStr != null) {
                            newPriority = Double.parseDouble(newPriorityStr);
                        }

                        if (newPriority > priority) {
                            if (isDiagnosticsEnabled()) {
                                logDiagnostic("[LOOKUP] Properties file at '" + url + "'" +
                                              " with priority " + newPriority +
                                              " overrides file at '" + propsUrl + "'" +
                                              " with priority " + priority);
                            }

                            propsUrl = url;
                            props = newProps;
                            priority = newPriority;
                        } else {
                            if (isDiagnosticsEnabled()) {
                                logDiagnostic("[LOOKUP] Properties file at '" + url + "'" +
                                              " with priority " + newPriority +
                                              " does not override file at '" + propsUrl + "'" +
                                              " with priority " + priority);
                            }
                        }
                    }

                }
            }
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("SecurityException thrown while trying to find/read config files.");
            }
        }

        if (isDiagnosticsEnabled()) {
            if (props == null) {
                logDiagnostic("[LOOKUP] No properties file of name '" + fileName + "' found.");
            } else {
                logDiagnostic("[LOOKUP] Properties file of name '" + fileName + "' found at '" + propsUrl + '"');
            }
        }

        return props;
    }

    /**
     * Read the specified system property, using an AccessController so that
     * the property can be read if JCL has been granted the appropriate
     * security rights even if the calling code has not.
     * <p>
     * Take care not to expose the value returned by this method to the
     * calling application in any way; otherwise the calling app can use that
     * info to access data that should not be available to it.
     */
    private static String getSystemProperty(final String key, final String def)
        throws SecurityException {
        return (String) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return System.getProperty(key, def);
                    }
                });
    }

    /**
     * Determines whether the user wants internal diagnostic output. If so,
     * returns an appropriate writer object. Users can enable diagnostic
     * output by setting the system property named {@link #DIAGNOSTICS_DEST_PROPERTY} to
     * a filename, or the special values STDOUT or STDERR.
     */
    private static PrintStream initDiagnostics() {
        String dest;
        try {
            dest = getSystemProperty(DIAGNOSTICS_DEST_PROPERTY, null);
            if (dest == null) {
                return null;
            }
        } catch (SecurityException ex) {
            // We must be running in some very secure environment.
            // We just have to assume output is not wanted..
            return null;
        }

        if (dest.equals("STDOUT")) {
            return System.out;
        } else if (dest.equals("STDERR")) {
            return System.err;
        } else {
            try {
                // open the file in append mode
                FileOutputStream fos = new FileOutputStream(dest, true);
                return new PrintStream(fos);
            } catch (IOException ex) {
                // We should report this to the user - but how?
                return null;
            }
        }
    }

    /**
     * Indicates true if the user has enabled internal logging.
     * <p>
     * By the way, sorry for the incorrect grammar, but calling this method
     * areDiagnosticsEnabled just isn't java beans style.
     *
     * @return true if calls to logDiagnostic will have any effect.
     * @since 1.1
     */
    protected static boolean isDiagnosticsEnabled() {
        return diagnosticsStream != null;
    }

    /**
     * Write the specified message to the internal logging destination.
     * <p>
     * Note that this method is private; concrete subclasses of this class
     * should not call it because the diagnosticPrefix string this
     * method puts in front of all its messages is LogFactory@....,
     * while subclasses should put SomeSubClass@...
     * <p>
     * Subclasses should instead compute their own prefix, then call
     * logRawDiagnostic. Note that calling isDiagnosticsEnabled is
     * fine for subclasses.
     * <p>
     * Note that it is safe to call this method before initDiagnostics
     * is called; any output will just be ignored (as isDiagnosticsEnabled
     * will return false).
     *
     * @param msg is the diagnostic message to be output.
     */
    private static final void logDiagnostic(String msg) {
        if (diagnosticsStream != null) {
            diagnosticsStream.print(diagnosticPrefix);
            diagnosticsStream.println(msg);
            diagnosticsStream.flush();
        }
    }

    /**
     * Write the specified message to the internal logging destination.
     *
     * @param msg is the diagnostic message to be output.
     * @since 1.1
     */
    protected static final void logRawDiagnostic(String msg) {
        if (diagnosticsStream != null) {
            diagnosticsStream.println(msg);
            diagnosticsStream.flush();
        }
    }

    /**
     * Generate useful diagnostics regarding the classloader tree for
     * the specified class.
     * <p>
     * As an example, if the specified class was loaded via a webapp's
     * classloader, then you may get the following output:
     * <pre>
     * Class com.acme.Foo was loaded via classloader 11111
     * ClassLoader tree: 11111 -> 22222 (SYSTEM) -> 33333 -> BOOT
     * </pre>
     * <p>
     * This method returns immediately if isDiagnosticsEnabled()
     * returns false.
     *
     * @param clazz is the class whose classloader + tree are to be
     * output.
     */
    private static void logClassLoaderEnvironment(Class clazz) {
        if (!isDiagnosticsEnabled()) {
            return;
        }

        try {
            // Deliberately use System.getProperty here instead of getSystemProperty; if
            // the overall security policy for the calling application forbids access to
            // these variables then we do not want to output them to the diagnostic stream.
            logDiagnostic("[ENV] Extension directories (java.ext.dir): " + System.getProperty("java.ext.dir"));
            logDiagnostic("[ENV] Application classpath (java.class.path): " + System.getProperty("java.class.path"));
        } catch (SecurityException ex) {
            logDiagnostic("[ENV] Security setting prevent interrogation of system classpaths.");
        }

        String className = clazz.getName();
        ClassLoader classLoader;

        try {
            classLoader = getClassLoader(clazz);
        } catch (SecurityException ex) {
            // not much useful diagnostics we can print here!
            logDiagnostic("[ENV] Security forbids determining the classloader for " + className);
            return;
        }

        logDiagnostic("[ENV] Class " + className + " was loaded via classloader " + objectId(classLoader));
        logHierarchy("[ENV] Ancestry of classloader which loaded " + className + " is ", classLoader);
    }

    /**
     * Logs diagnostic messages about the given classloader
     * and it's hierarchy. The prefix is prepended to the message
     * and is intended to make it easier to understand the logs.
     * @param prefix
     * @param classLoader
     */
    private static void logHierarchy(String prefix, ClassLoader classLoader) {
        if (!isDiagnosticsEnabled()) {
            return;
        }
        ClassLoader systemClassLoader;
        if (classLoader != null) {
            final String classLoaderString = classLoader.toString();
            logDiagnostic(prefix + objectId(classLoader) + " == '" + classLoaderString + "'");
        }

        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ex) {
            logDiagnostic(prefix + "Security forbids determining the system classloader.");
            return;
        }
        if (classLoader != null) {
            final StringBuffer buf = new StringBuffer(prefix + "ClassLoader tree:");
            for(;;) {
                buf.append(objectId(classLoader));
                if (classLoader == systemClassLoader) {
                    buf.append(" (SYSTEM) ");
                }

                try {
                    classLoader = classLoader.getParent();
                } catch (SecurityException ex) {
                    buf.append(" --> SECRET");
                    break;
                }

                buf.append(" --> ");
                if (classLoader == null) {
                    buf.append("BOOT");
                    break;
                }
            }
            logDiagnostic(buf.toString());
        }
    }

    /**
     * Returns a string that uniquely identifies the specified object, including
     * its class.
     * <p>
     * The returned string is of form "classname@hashcode", ie is the same as
     * the return value of the Object.toString() method, but works even when
     * the specified object's class has overidden the toString method.
     *
     * @param o may be null.
     * @return a string of form classname@hashcode, or "null" if param o is null.
     * @since 1.1
     */
    public static String objectId(Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + System.identityHashCode(o);
        }
    }

    // ----------------------------------------------------------------------
    // Static initialiser block to perform initialisation at class load time.
    //
    // We can't do this in the class constructor, as there are many
    // static methods on this class that can be called before any
    // LogFactory instances are created, and they depend upon this
    // stuff having been set up.
    //
    // Note that this block must come after any variable declarations used
    // by any methods called from this block, as we want any static initialiser
    // associated with the variable to run first. If static initialisers for
    // variables run after this code, then (a) their value might be needed
    // by methods called from here, and (b) they might *override* any value
    // computed here!
    //
    // So the wisest thing to do is just to place this code at the very end
    // of the class file.
    // ----------------------------------------------------------------------

    static {
        // note: it's safe to call methods before initDiagnostics (though
        // diagnostic output gets discarded).
        thisClassLoader = getClassLoader(LogFactory.class);
        // In order to avoid confusion where multiple instances of JCL are
        // being used via different classloaders within the same app, we
        // ensure each logged message has a prefix of form
        // [LogFactory from classloader OID]
        //
        // Note that this prefix should be kept consistent with that
        // in LogFactoryImpl. However here we don't need to output info
        // about the actual *instance* of LogFactory, as all methods that
        // output diagnostics from this class are static.
        String classLoaderName;
        try {
            ClassLoader classLoader = thisClassLoader;
            if (thisClassLoader == null) {
                classLoaderName = "BOOTLOADER";
            } else {
                classLoaderName = objectId(classLoader);
            }
        } catch (SecurityException e) {
            classLoaderName = "UNKNOWN";
        }
        diagnosticPrefix = "[LogFactory from " + classLoaderName + "] ";
        diagnosticsStream = initDiagnostics();
        logClassLoaderEnvironment(LogFactory.class);
        factories = createFactoryStore();
        if (isDiagnosticsEnabled()) {
            logDiagnostic("BOOTSTRAP COMPLETED");
        }
    }
}
