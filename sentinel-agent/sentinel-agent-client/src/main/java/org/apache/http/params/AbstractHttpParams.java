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

import java.util.Set;

/**
 * Abstract base class for parameter collections.
 * Type specific setters and getters are mapped to the abstract,
 * generic getters and setters.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public abstract class AbstractHttpParams implements HttpParams, HttpParamsNames {

    /**
     * Instantiates parameters.
     */
    protected AbstractHttpParams() {
        super();
    }

    @Override
    public long getLongParameter(final String name, final long defaultValue) {
        final Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Long) param).longValue();
    }

    @Override
    public HttpParams setLongParameter(final String name, final long value) {
        setParameter(name, Long.valueOf(value));
        return this;
    }

    @Override
    public int getIntParameter(final String name, final int defaultValue) {
        final Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Integer) param).intValue();
    }

    @Override
    public HttpParams setIntParameter(final String name, final int value) {
        setParameter(name, Integer.valueOf(value));
        return this;
    }

    @Override
    public double getDoubleParameter(final String name, final double defaultValue) {
        final Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Double) param).doubleValue();
    }

    @Override
    public HttpParams setDoubleParameter(final String name, final double value) {
        setParameter(name, Double.valueOf(value));
        return this;
    }

    @Override
    public boolean getBooleanParameter(final String name, final boolean defaultValue) {
        final Object param = getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return ((Boolean) param).booleanValue();
    }

    @Override
    public HttpParams setBooleanParameter(final String name, final boolean value) {
        setParameter(name, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    @Override
    public boolean isParameterTrue(final String name) {
        return getBooleanParameter(name, false);
    }

    @Override
    public boolean isParameterFalse(final String name) {
        return !getBooleanParameter(name, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Dummy implementation - must be overridden by subclasses.
     *
     * @since 4.2
     * @throws UnsupportedOperationException - always
     */
    @Override
    public Set<String> getNames(){
        throw new UnsupportedOperationException();
    }

} // class AbstractHttpParams
