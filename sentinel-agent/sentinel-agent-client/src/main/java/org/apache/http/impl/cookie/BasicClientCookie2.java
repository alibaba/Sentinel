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

import java.util.Date;

import org.apache.http.cookie.SetCookie2;

/**
 * Default implementation of {@link SetCookie2}.
 *
 * @since 4.0
 */
public class BasicClientCookie2 extends BasicClientCookie implements SetCookie2 {

    private static final long serialVersionUID = -7744598295706617057L;

    private String commentURL;
    private int[] ports;
    private boolean discard;

    /**
     * Default Constructor taking a name and a value. The value may be null.
     *
     * @param name The name.
     * @param value The value.
     */
    public BasicClientCookie2(final String name, final String value) {
        super(name, value);
    }

    @Override
    public int[] getPorts() {
        return this.ports;
    }

    @Override
    public void setPorts(final int[] ports) {
        this.ports = ports;
    }

    @Override
    public String getCommentURL() {
        return this.commentURL;
    }

    @Override
    public void setCommentURL(final String commentURL) {
        this.commentURL = commentURL;
    }

    @Override
    public void setDiscard(final boolean discard) {
        this.discard = discard;
    }

    @Override
    public boolean isPersistent() {
        return !this.discard && super.isPersistent();
    }

    @Override
    public boolean isExpired(final Date date) {
        return this.discard || super.isExpired(date);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final BasicClientCookie2 clone = (BasicClientCookie2) super.clone();
        if (this.ports != null) {
            clone.ports = this.ports.clone();
        }
        return clone;
    }

}

