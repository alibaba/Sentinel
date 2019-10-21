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

import java.util.Comparator;
import java.util.Date;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * This cookie comparator ensures that cookies with longer paths take precedence over
 * cookies with shorter path. Among cookies with equal path length cookies with ealier
 * creation time take precedence over cookies with later creation time
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class CookiePriorityComparator implements Comparator<Cookie> {

    public static final CookiePriorityComparator INSTANCE = new CookiePriorityComparator();

    private int getPathLength(final Cookie cookie) {
        final String path = cookie.getPath();
        return path != null ? path.length() : 1;
    }

    @Override
    public int compare(final Cookie c1, final Cookie c2) {
        final int l1 = getPathLength(c1);
        final int l2 = getPathLength(c2);
        //TODO: update this class once Cookie interface has been expended with #getCreationTime method
        final int result = l2 - l1;
        if (result == 0 && c1 instanceof BasicClientCookie && c2 instanceof BasicClientCookie) {
            final Date d1 = ((BasicClientCookie) c1).getCreationDate();
            final Date d2 = ((BasicClientCookie) c2).getCreationDate();
            if (d1 != null && d2 != null) {
                return (int) (d1.getTime() - d2.getTime());
            }
        }
        return result;
    }

}
