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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;

/**
 * Default implementation of {@link SetCookie}.
 *
 * @since 4.0
 */
public class BasicClientCookie implements SetCookie, ClientCookie, Cloneable, Serializable {

    private static final long serialVersionUID = -3869795591041535538L;

    /**
     * Default Constructor taking a name and a value. The value may be null.
     *
     * @param name The name.
     * @param value The value.
     */
    public BasicClientCookie(final String name, final String value) {
        super();
        Args.notNull(name, "Name");
        this.name = name;
        this.attribs = new HashMap<String, String>();
        this.value = value;
    }

    /**
     * Returns the name.
     *
     * @return String name The name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value.
     *
     * @return String value The current value.
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value
     *
     * @param value
     */
    @Override
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * {@code null} if no such comment has been defined.
     *
     * @return comment
     *
     * @see #setComment(String)
     */
    @Override
    public String getComment() {
        return cookieComment;
    }

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.
     *
     * @param comment
     *
     * @see #getComment()
     */
    @Override
    public void setComment(final String comment) {
        cookieComment = comment;
    }


    /**
     * Returns null. Cookies prior to RFC2965 do not set this attribute
     */
    @Override
    public String getCommentURL() {
        return null;
    }


    /**
     * Returns the expiration {@link Date} of the cookie, or {@code null}
     * if none exists.
     * <p><strong>Note:</strong> the object returned by this method is
     * considered immutable. Changing it (e.g. using setTime()) could result
     * in undefined behaviour. Do so at your peril. </p>
     * @return Expiration {@link Date}, or {@code null}.
     *
     * @see #setExpiryDate(java.util.Date)
     *
     */
    @Override
    public Date getExpiryDate() {
        return cookieExpiryDate;
    }

    /**
     * Sets expiration date.
     * <p><strong>Note:</strong> the object returned by this method is considered
     * immutable. Changing it (e.g. using setTime()) could result in undefined
     * behaviour. Do so at your peril.</p>
     *
     * @param expiryDate the {@link Date} after which this cookie is no longer valid.
     *
     * @see #getExpiryDate
     *
     */
    @Override
    public void setExpiryDate (final Date expiryDate) {
        cookieExpiryDate = expiryDate;
    }


    /**
     * Returns {@code false} if the cookie should be discarded at the end
     * of the "session"; {@code true} otherwise.
     *
     * @return {@code false} if the cookie should be discarded at the end
     *         of the "session"; {@code true} otherwise
     */
    @Override
    public boolean isPersistent() {
        return (null != cookieExpiryDate);
    }


    /**
     * Returns domain attribute of the cookie.
     *
     * @return the value of the domain attribute
     *
     * @see #setDomain(java.lang.String)
     */
    @Override
    public String getDomain() {
        return cookieDomain;
    }

    /**
     * Sets the domain attribute.
     *
     * @param domain The value of the domain attribute
     *
     * @see #getDomain
     */
    @Override
    public void setDomain(final String domain) {
        if (domain != null) {
            cookieDomain = domain.toLowerCase(Locale.ROOT);
        } else {
            cookieDomain = null;
        }
    }


    /**
     * Returns the path attribute of the cookie
     *
     * @return The value of the path attribute.
     *
     * @see #setPath(java.lang.String)
     */
    @Override
    public String getPath() {
        return cookiePath;
    }

    /**
     * Sets the path attribute.
     *
     * @param path The value of the path attribute
     *
     * @see #getPath
     *
     */
    @Override
    public void setPath(final String path) {
        cookiePath = path;
    }

    /**
     * @return {@code true} if this cookie should only be sent over secure connections.
     * @see #setSecure(boolean)
     */
    @Override
    public boolean isSecure() {
        return isSecure;
    }

    /**
     * Sets the secure attribute of the cookie.
     * <p>
     * When {@code true} the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @param secure The value of the secure attribute
     *
     * @see #isSecure()
     */
    @Override
    public void setSecure (final boolean secure) {
        isSecure = secure;
    }


    /**
     * Returns null. Cookies prior to RFC2965 do not set this attribute
     */
    @Override
    public int[] getPorts() {
        return null;
    }


    /**
     * Returns the version of the cookie specification to which this
     * cookie conforms.
     *
     * @return the version of the cookie.
     *
     * @see #setVersion(int)
     *
     */
    @Override
    public int getVersion() {
        return cookieVersion;
    }

    /**
     * Sets the version of the cookie specification to which this
     * cookie conforms.
     *
     * @param version the version of the cookie.
     *
     * @see #getVersion
     */
    @Override
    public void setVersion(final int version) {
        cookieVersion = version;
    }

    /**
     * Returns true if this cookie has expired.
     * @param date Current time
     *
     * @return {@code true} if the cookie has expired.
     */
    @Override
    public boolean isExpired(final Date date) {
        Args.notNull(date, "Date");
        return (cookieExpiryDate != null
            && cookieExpiryDate.getTime() <= date.getTime());
    }

    /**
     * @since 4.4
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @since 4.4
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setAttribute(final String name, final String value) {
        this.attribs.put(name, value);
    }

    @Override
    public String getAttribute(final String name) {
        return this.attribs.get(name);
    }

    @Override
    public boolean containsAttribute(final String name) {
        return this.attribs.containsKey(name);
    }

    /**
     * @since 4.4
     */
    public boolean removeAttribute(final String name) {
        return this.attribs.remove(name) != null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final BasicClientCookie clone = (BasicClientCookie) super.clone();
        clone.attribs = new HashMap<String, String>(this.attribs);
        return clone;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[version: ");
        buffer.append(Integer.toString(this.cookieVersion));
        buffer.append("]");
        buffer.append("[name: ");
        buffer.append(this.name);
        buffer.append("]");
        buffer.append("[value: ");
        buffer.append(this.value);
        buffer.append("]");
        buffer.append("[domain: ");
        buffer.append(this.cookieDomain);
        buffer.append("]");
        buffer.append("[path: ");
        buffer.append(this.cookiePath);
        buffer.append("]");
        buffer.append("[expiry: ");
        buffer.append(this.cookieExpiryDate);
        buffer.append("]");
        return buffer.toString();
    }

   // ----------------------------------------------------- Instance Variables

    /** Cookie name */
    private final String name;

    /** Cookie attributes as specified by the origin server */
    private Map<String, String> attribs;

    /** Cookie value */
    private String value;

    /** Comment attribute. */
    private String  cookieComment;

    /** Domain attribute. */
    private String  cookieDomain;

    /** Expiration {@link Date}. */
    private Date cookieExpiryDate;

    /** Path attribute. */
    private String cookiePath;

    /** My secure flag. */
    private boolean isSecure;

    /** The version of the cookie specification I was created from. */
    private int cookieVersion;

    private Date creationDate;

}

