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

import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;

/**
 * Thread-safe extension of the {@link BasicHttpParams}.
 *
 * @since 4.1
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Contract(threading = ThreadingBehavior.SAFE)
@Deprecated
public class SyncBasicHttpParams extends BasicHttpParams {

    private static final long serialVersionUID = 5387834869062660642L;

    public SyncBasicHttpParams() {
        super();
    }

    @Override
    public synchronized boolean removeParameter(final String name) {
        return super.removeParameter(name);
    }

    @Override
    public synchronized HttpParams setParameter(final String name, final Object value) {
        return super.setParameter(name, value);
    }

    @Override
    public synchronized Object getParameter(final String name) {
        return super.getParameter(name);
    }

    @Override
    public synchronized boolean isParameterSet(final String name) {
        return super.isParameterSet(name);
    }

    @Override
    public synchronized boolean isParameterSetLocally(final String name) {
        return super.isParameterSetLocally(name);
    }

    @Override
    public synchronized void setParameters(final String[] names, final Object value) {
        super.setParameters(names, value);
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public synchronized Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
