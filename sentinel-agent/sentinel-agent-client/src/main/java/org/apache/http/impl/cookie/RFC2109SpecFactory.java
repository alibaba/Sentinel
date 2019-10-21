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

package org.apache.http.impl.cookie;

import java.util.Collection;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * {@link org.apache.http.cookie.CookieSpecProvider} implementation that provides an instance of
 * {@link org.apache.http.impl.cookie.RFC2109Spec}. The instance returned by this factory
 * can be shared by multiple threads.
 *
 * @deprecated (4.4) Use {@link org.apache.http.impl.cookie.RFC2109SpecProvider}.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public class RFC2109SpecFactory implements CookieSpecFactory, CookieSpecProvider {

    private final CookieSpec cookieSpec;

    public RFC2109SpecFactory(final String[] datepatterns, final boolean oneHeader) {
        super();
        this.cookieSpec = new RFC2109Spec(datepatterns, oneHeader);
    }

    public RFC2109SpecFactory() {
        this(null, false);
    }

    @Override
    public CookieSpec newInstance(final HttpParams params) {
        if (params != null) {

            String[] patterns = null;
            final Collection<?> param = (Collection<?>) params.getParameter(
                    CookieSpecPNames.DATE_PATTERNS);
            if (param != null) {
                patterns = new String[param.size()];
                patterns = param.toArray(patterns);
            }
            final boolean singleHeader = params.getBooleanParameter(
                    CookieSpecPNames.SINGLE_COOKIE_HEADER, false);

            return new RFC2109Spec(patterns, singleHeader);
        } else {
            return new RFC2109Spec();
        }
    }

    @Override
    public CookieSpec create(final HttpContext context) {
        return this.cookieSpec;
    }

}
