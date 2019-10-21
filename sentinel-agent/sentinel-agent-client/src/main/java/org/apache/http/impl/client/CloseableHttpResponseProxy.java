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

package org.apache.http.impl.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

/**
 * @since 4.3
 */
@Deprecated
class CloseableHttpResponseProxy implements InvocationHandler {

    private final static Constructor<?> CONSTRUCTOR;

    static {
        try {
            CONSTRUCTOR = Proxy.getProxyClass(CloseableHttpResponseProxy.class.getClassLoader(),
                    new Class<?>[] { CloseableHttpResponse.class }).getConstructor(new Class[] { InvocationHandler.class });
        } catch (final NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private final HttpResponse original;

    CloseableHttpResponseProxy(final HttpResponse original) {
        super();
        this.original = original;
    }

    public void close() throws IOException {
        final HttpEntity entity = this.original.getEntity();
        EntityUtils.consume(entity);
    }

    @Override
    public Object invoke(
            final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String mname = method.getName();
        if (mname.equals("close")) {
            close();
            return null;
        } else {
            try {
                return method.invoke(original, args);
            } catch (final InvocationTargetException ex) {
                final Throwable cause = ex.getCause();
                if (cause != null) {
                    throw cause;
                } else {
                    throw ex;
                }
            }
        }
    }

    public static CloseableHttpResponse newProxy(final HttpResponse original) {
        try {
            return (CloseableHttpResponse) CONSTRUCTOR.newInstance(new CloseableHttpResponseProxy(original));
        } catch (final InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
