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

package org.apache.http.params;

import org.apache.http.HttpVersion;

/**
 * This is a Java Bean class that can be used to wrap an instance of
 * {@link HttpParams} and manipulate HTTP protocol parameters using Java Beans
 * conventions.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public class HttpProtocolParamBean extends HttpAbstractParamBean {

    public HttpProtocolParamBean (final HttpParams params) {
        super(params);
    }

    public void setHttpElementCharset (final String httpElementCharset) {
        HttpProtocolParams.setHttpElementCharset(params, httpElementCharset);
    }

    public void setContentCharset (final String contentCharset) {
        HttpProtocolParams.setContentCharset(params, contentCharset);
    }

    public void setVersion (final HttpVersion version) {
        HttpProtocolParams.setVersion(params, version);
    }

    public void setUserAgent (final String userAgent) {
        HttpProtocolParams.setUserAgent(params, userAgent);
    }

    public void setUseExpectContinue (final boolean useExpectContinue) {
        HttpProtocolParams.setUseExpectContinue(params, useExpectContinue);
    }

}
