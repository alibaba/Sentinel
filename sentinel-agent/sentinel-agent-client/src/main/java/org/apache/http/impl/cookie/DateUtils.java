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
import java.util.TimeZone;

/**
 * A utility class for parsing and formatting HTTP dates as used in cookies and
 * other headers.  This class handles dates as defined by RFC 2616 section
 * 3.3.1 as well as some other common non-standard formats.
 *
 *
 * @since 4.0
 *
 * @deprecated (4.3) Use {@link org.apache.http.client.utils.DateUtils}.
 */
@Deprecated
public final class DateUtils {

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = org.apache.http.client.utils.DateUtils.PATTERN_RFC1123;

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = org.apache.http.client.utils.DateUtils.PATTERN_RFC1036;

    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * {@code asctime()} format.
     */
    public static final String PATTERN_ASCTIME = org.apache.http.client.utils.DateUtils.PATTERN_ASCTIME;

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    /**
     * Parses a date value.  The formats used for parsing the date value are retrieved from
     * the default http params.
     *
     * @param dateValue the date value to parse
     *
     * @return the parsed date
     *
     * @throws DateParseException if the value could not be parsed using any of the
     * supported date formats
     */
    public static Date parseDate(final String dateValue) throws DateParseException {
        return parseDate(dateValue, null, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     *
     * @return the parsed date
     *
     * @throws DateParseException if none of the dataFormats could parse the dateValue
     */
    public static Date parseDate(final String dateValue, final String[] dateFormats)
        throws DateParseException {
        return parseDate(dateValue, dateFormats, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     * @param startDate During parsing, two digit years will be placed in the range
     * {@code startDate} to {@code startDate + 100 years}. This value may
     * be {@code null}. When {@code null} is given as a parameter, year
     * {@code 2000} will be used.
     *
     * @return the parsed date
     *
     * @throws DateParseException if none of the dataFormats could parse the dateValue
     */
    public static Date parseDate(
        final String dateValue,
        final String[] dateFormats,
        final Date startDate
    ) throws DateParseException {
        final Date d = org.apache.http.client.utils.DateUtils.parseDate(dateValue, dateFormats, startDate);
        if (d == null) {
            throw new DateParseException("Unable to parse the date " + dateValue);
        }
        return d;
    }

    /**
     * Formats the given date according to the RFC 1123 pattern.
     *
     * @param date The date to format.
     * @return An RFC 1123 formatted date string.
     *
     * @see #PATTERN_RFC1123
     */
    public static String formatDate(final Date date) {
        return org.apache.http.client.utils.DateUtils.formatDate(date);
    }

    /**
     * Formats the given date according to the specified pattern.  The pattern
     * must conform to that used by the {@link java.text.SimpleDateFormat simple
     * date format} class.
     *
     * @param date The date to format.
     * @param pattern The pattern to use for formatting the date.
     * @return A formatted date string.
     *
     * @throws IllegalArgumentException If the given date pattern is invalid.
     *
     * @see java.text.SimpleDateFormat
     */
    public static String formatDate(final Date date, final String pattern) {
        return org.apache.http.client.utils.DateUtils.formatDate(date, pattern);
    }

    /** This class should not be instantiated. */
    private DateUtils() {
    }

}
