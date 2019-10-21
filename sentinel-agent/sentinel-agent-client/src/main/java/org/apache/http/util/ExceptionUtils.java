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

import java.lang.reflect.Method;

/**
 * The home for utility methods that handle various exception-related tasks.
 *
 *
 * @since 4.0
 *
 * @deprecated (4.2) no longer used
 */
@Deprecated
public final class ExceptionUtils {

    /** A reference to Throwable's initCause method, or null if it's not there in this JVM */
    static private final Method INIT_CAUSE_METHOD = getInitCauseMethod();

    /**
     * Returns a {@code Method} allowing access to
     * {@link Throwable#initCause(Throwable) initCause} method of {@link Throwable},
     * or {@code null} if the method
     * does not exist.
     *
     * @return A {@code Method} for {@code Throwable.initCause}, or
     * {@code null} if unavailable.
     */
    static private Method getInitCauseMethod() {
        try {
            final Class<?>[] paramsClasses = new Class[] { Throwable.class };
            return Throwable.class.getMethod("initCause", paramsClasses);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * If we're running on JDK 1.4 or later, initialize the cause for the given throwable.
     *
     * @param  throwable The throwable.
     * @param  cause     The cause of the throwable.
     */
    public static void initCause(final Throwable throwable, final Throwable cause) {
        if (INIT_CAUSE_METHOD != null) {
            try {
                INIT_CAUSE_METHOD.invoke(throwable, cause);
            } catch (final Exception e) {
                // Well, with no logging, the only option is to munch the exception
            }
        }
    }

    private ExceptionUtils() {
    }

}
