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

package org.apache.http.auth.params;

import org.apache.http.params.HttpAbstractParamBean;
import org.apache.http.params.HttpParams;

/**
 * This is a Java Bean class that can be used to wrap an instance of
 * {@link HttpParams} and manipulate HTTP authentication parameters
 * using Java Beans conventions.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link org.apache.http.client.config.RequestConfig}
 *   and constructor parameters of
 *   {@link org.apache.http.auth.AuthSchemeProvider}s.
 */
@Deprecated
public class AuthParamBean extends HttpAbstractParamBean {

    public AuthParamBean (final HttpParams params) {
        super(params);
    }

    public void setCredentialCharset (final String charset) {
        AuthParams.setCredentialCharset(params, charset);
    }

}
