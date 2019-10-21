/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides access to version information for HTTP components.
 * Static methods are used to extract version information from property
 * files that are automatically packaged with HTTP component release JARs.
 * <p>
 * All available version information is provided in strings, where
 * the string format is informal and subject to change without notice.
 * Version information is provided for debugging output and interpretation
 * by humans, not for automated processing in applications.
 * </p>
 *
 * @since 4.0
 */
public class VersionInfo {

    /** A string constant for unavailable information. */
    public final static String UNAVAILABLE = "UNAVAILABLE";

    /** The filename of the version information files. */
    public final static String VERSION_PROPERTY_FILE = "version.properties";

    // the property names
    public final static String PROPERTY_MODULE    = "info.module";
    public final static String PROPERTY_RELEASE   = "info.release";
    public final static String PROPERTY_TIMESTAMP = "info.timestamp";


    /** The package that contains the version information. */
    private final String infoPackage;

    /** The module from the version info. */
    private final String infoModule;

    /** The release from the version info. */
    private final String infoRelease;

    /** The timestamp from the version info. */
    private final String infoTimestamp;

    /** The classloader from which the version info was obtained. */
    private final String infoClassloader;


    /**
     * Instantiates version information.
     *
     * @param pckg      the package
     * @param module    the module, or {@code null}
     * @param release   the release, or {@code null}
     * @param time      the build time, or {@code null}
     * @param clsldr    the class loader, or {@code null}
     */
    protected VersionInfo(final String pckg, final String module,
                          final String release, final String time, final String clsldr) {
        Args.notNull(pckg, "Package identifier");
        infoPackage     = pckg;
        infoModule      = (module  != null) ? module  : UNAVAILABLE;
        infoRelease     = (release != null) ? release : UNAVAILABLE;
        infoTimestamp   = (time    != null) ? time    : UNAVAILABLE;
        infoClassloader = (clsldr  != null) ? clsldr  : UNAVAILABLE;
    }


    /**
     * Obtains the package name.
     * The package name identifies the module or informal unit.
     *
     * @return  the package name, never {@code null}
     */
    public final String getPackage() {
        return infoPackage;
    }

    /**
     * Obtains the name of the versioned module or informal unit.
     * This data is read from the version information for the package.
     *
     * @return  the module name, never {@code null}
     */
    public final String getModule() {
        return infoModule;
    }

    /**
     * Obtains the release of the versioned module or informal unit.
     * This data is read from the version information for the package.
     *
     * @return  the release version, never {@code null}
     */
    public final String getRelease() {
        return infoRelease;
    }

    /**
     * Obtains the timestamp of the versioned module or informal unit.
     * This data is read from the version information for the package.
     *
     * @return  the timestamp, never {@code null}
     */
    public final String getTimestamp() {
        return infoTimestamp;
    }

    /**
     * Obtains the classloader used to read the version information.
     * This is just the {@code toString} output of the classloader,
     * since the version information should not keep a reference to
     * the classloader itself. That could prevent garbage collection.
     *
     * @return  the classloader description, never {@code null}
     */
    public final String getClassloader() {
        return infoClassloader;
    }


    /**
     * Provides the version information in human-readable format.
     *
     * @return  a string holding this version information
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder
            (20 + infoPackage.length() + infoModule.length() +
             infoRelease.length() + infoTimestamp.length() +
             infoClassloader.length());

        sb.append("VersionInfo(")
            .append(infoPackage).append(':').append(infoModule);

        // If version info is missing, a single "UNAVAILABLE" for the module
        // is sufficient. Everything else just clutters the output.
        if (!UNAVAILABLE.equals(infoRelease)) {
            sb.append(':').append(infoRelease);
        }
        if (!UNAVAILABLE.equals(infoTimestamp)) {
            sb.append(':').append(infoTimestamp);
        }

        sb.append(')');

        if (!UNAVAILABLE.equals(infoClassloader)) {
            sb.append('@').append(infoClassloader);
        }

        return sb.toString();
    }


    /**
     * Loads version information for a list of packages.
     *
     * @param pckgs     the packages for which to load version info
     * @param clsldr    the classloader to load from, or
     *                  {@code null} for the thread context classloader
     *
     * @return  the version information for all packages found,
     *          never {@code null}
     */
    public static VersionInfo[] loadVersionInfo(final String[] pckgs,
                                                      final ClassLoader clsldr) {
        Args.notNull(pckgs, "Package identifier array");
        final List<VersionInfo> vil = new ArrayList<VersionInfo>(pckgs.length);
        for (final String pckg : pckgs) {
            final VersionInfo vi = loadVersionInfo(pckg, clsldr);
            if (vi != null) {
                vil.add(vi);
            }
        }

        return vil.toArray(new VersionInfo[vil.size()]);
    }


    /**
     * Loads version information for a package.
     *
     * @param pckg      the package for which to load version information,
     *                  for example "org.apache.http".
     *                  The package name should NOT end with a dot.
     * @param clsldr    the classloader to load from, or
     *                  {@code null} for the thread context classloader
     *
     * @return  the version information for the argument package, or
     *          {@code null} if not available
     */
    public static VersionInfo loadVersionInfo(final String pckg,
                                              final ClassLoader clsldr) {
        Args.notNull(pckg, "Package identifier");
        final ClassLoader cl = clsldr != null ? clsldr : Thread.currentThread().getContextClassLoader();

        Properties vip = null; // version info properties, if available
        try {
            // org.apache.http      becomes
            // org/apache/http/version.properties
            final InputStream is = cl.getResourceAsStream
                (pckg.replace('.', '/') + "/" + VERSION_PROPERTY_FILE);
            if (is != null) {
                try {
                    final Properties props = new Properties();
                    props.load(is);
                    vip = props;
                } finally {
                    is.close();
                }
            }
        } catch (final IOException ex) {
            // shamelessly munch this exception
        }

        VersionInfo result = null;
        if (vip != null) {
            result = fromMap(pckg, vip, cl);
        }

        return result;
    }


    /**
     * Instantiates version information from properties.
     *
     * @param pckg      the package for the version information
     * @param info      the map from string keys to string values,
     *                  for example {@link java.util.Properties}
     * @param clsldr    the classloader, or {@code null}
     *
     * @return  the version information
     */
    protected static VersionInfo fromMap(final String pckg, final Map<?, ?> info,
                                               final ClassLoader clsldr) {
        Args.notNull(pckg, "Package identifier");
        String module = null;
        String release = null;
        String timestamp = null;

        if (info != null) {
            module = (String) info.get(PROPERTY_MODULE);
            if ((module != null) && (module.length() < 1)) {
                module = null;
            }

            release = (String) info.get(PROPERTY_RELEASE);
            if ((release != null) && ((release.length() < 1) ||
                                      (release.equals("${pom.version}")))) {
                release = null;
            }

            timestamp = (String) info.get(PROPERTY_TIMESTAMP);
            if ((timestamp != null) &&
                ((timestamp.length() < 1) ||
                 (timestamp.equals("${mvn.timestamp}")))
                ) {
                timestamp = null;
            }
        } // if info

        String clsldrstr = null;
        if (clsldr != null) {
            clsldrstr = clsldr.toString();
        }

        return new VersionInfo(pckg, module, release, timestamp, clsldrstr);
    }

    /**
     * Sets the user agent to {@code "<name>/<release> (Java/<java.version>)"}.
     * <p>
     * For example:
     * <pre>"Apache-HttpClient/4.3 (Java/1.6.0_35)"</pre>
     *
     * @param name the component name, like "Apache-HttpClient".
     * @param pkg
     *            the package for which to load version information, for example "org.apache.http". The package name
     *            should NOT end with a dot.
     * @param cls
     *            the class' class loader to load from, or {@code null} for the thread context class loader
     * @since 4.3
     */
    public static String getUserAgent(final String name, final String pkg, final Class<?> cls) {
        // determine the release version from packaged version info
        final VersionInfo vi = VersionInfo.loadVersionInfo(pkg, cls.getClassLoader());
        final String release = (vi != null) ? vi.getRelease() : VersionInfo.UNAVAILABLE;
        final String javaVersion = System.getProperty("java.version");

        return String.format("%s/%s (Java/%s)", name, release, javaVersion);
    }

} // class VersionInfo
