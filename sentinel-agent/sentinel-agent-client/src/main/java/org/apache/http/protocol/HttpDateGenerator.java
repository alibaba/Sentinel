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

package org.apache.http.protocol;

import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Generates a date in the format required by the HTTP protocol.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class HttpDateGenerator {

    /** Date format pattern used to generate the header in RFC 1123 format. */
    public static final
        String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /** The time zone to use in the date header. */
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private final DateFormat dateformat;
    private long dateAsLong = 0L;
    private String dateAsText = null;

    public HttpDateGenerator() {
        super();
        this.dateformat = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
        this.dateformat.setTimeZone(GMT);
    }

    public synchronized String getCurrentDate() {
        final long now = System.currentTimeMillis();
        if (now - this.dateAsLong > 1000) {
            // Generate new date string
            this.dateAsText = this.dateformat.format(new Date(now));
            this.dateAsLong = now;
        }
        return this.dateAsText;
    }

}
