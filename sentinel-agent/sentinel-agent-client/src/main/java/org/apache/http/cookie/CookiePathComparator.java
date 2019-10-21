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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;

/**
 * This cookie comparator ensures that multiple cookies satisfying
 * a common criteria are ordered in the {@code Cookie} header such
 * that those with more specific Path attributes precede those with
 * less specific.
 *
 * <p>
 * This comparator assumes that Path attributes of two cookies
 * path-match a commmon request-URI. Otherwise, the result of the
 * comparison is undefined.
 * </p>
 *
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class CookiePathComparator implements Serializable, Comparator<Cookie> {

    public static final CookiePathComparator INSTANCE = new CookiePathComparator();

    private static final long serialVersionUID = 7523645369616405818L;

    private String normalizePath(final Cookie cookie) {
        String path = cookie.getPath();
        if (path == null) {
            path = "/";
        }
        if (!path.endsWith("/")) {
            path = path + '/';
        }
        return path;
    }

    @Override
    public int compare(final Cookie c1, final Cookie c2) {
        final String path1 = normalizePath(c1);
        final String path2 = normalizePath(c2);
        if (path1.equals(path2)) {
            return 0;
        } else if (path1.startsWith(path2)) {
            return -1;
        } else if (path2.startsWith(path1)) {
            return 1;
        } else {
            // Does not really matter
            return 0;
        }
    }

}
