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
package org.apache.http.client.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;

/**
 * Uses the java.net.IDN class through reflection.
 *
 * @deprecated (4.4) use standard {@link java.net.IDN}.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public class JdkIdn implements Idn {
    private final Method toUnicode;

    /**
     *
     * @throws ClassNotFoundException if java.net.IDN is not available
     */
    public JdkIdn() throws ClassNotFoundException {
        final Class<?> clazz = Class.forName("java.net.IDN");
        try {
            toUnicode = clazz.getMethod("toUnicode", String.class);
        } catch (final SecurityException e) {
            // doesn't happen
            throw new IllegalStateException(e.getMessage(), e);
        } catch (final NoSuchMethodException e) {
            // doesn't happen
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public String toUnicode(final String punycode) {
        try {
            return (String) toUnicode.invoke(null, punycode);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (final InvocationTargetException e) {
            final Throwable t = e.getCause();
            throw new RuntimeException(t.getMessage(), t);
        }
    }

}
