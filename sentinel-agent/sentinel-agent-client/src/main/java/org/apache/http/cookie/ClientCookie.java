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

package org.apache.http.cookie;

import org.apache.http.annotation.Obsolete;

/**
 * ClientCookie extends the standard {@link Cookie} interface with
 * additional client specific functionality such ability to retrieve
 * original cookie attributes exactly as they were specified by the
 * origin server. This is important for generating the {@code Cookie}
 * header because some cookie specifications require that the
 * {@code Cookie} header should include certain attributes only if
 * they were specified in the {@code Set-Cookie} header.
 * <p>
 * Please do not use attributes marked as @Obsolete. They have been rendered
 * obsolete by RFC 6265.
 *
 * @since 4.0
 */
public interface ClientCookie extends Cookie {

    @Obsolete
    public static final String VERSION_ATTR    = "version";
    public static final String PATH_ATTR       = "path";
    public static final String DOMAIN_ATTR     = "domain";
    public static final String MAX_AGE_ATTR    = "max-age";
    public static final String SECURE_ATTR     = "secure";
    @Obsolete
    public static final String COMMENT_ATTR    = "comment";
    public static final String EXPIRES_ATTR    = "expires";

    @Obsolete
    public static final String PORT_ATTR       = "port";
    @Obsolete
    public static final String COMMENTURL_ATTR = "commenturl";
    @Obsolete
    public static final String DISCARD_ATTR    = "discard";

    String getAttribute(String name);

    boolean containsAttribute(String name);

}
