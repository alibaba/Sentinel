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
package org.apache.http.client.utils;

import java.util.StringTokenizer;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;

/**
 * Implementation from pseudo code in RFC 3492.
 *
 * @deprecated (4.4) use standard {@link java.net.IDN}.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public class Rfc3492Idn implements Idn {
    private static final int base = 36;
    private static final int tmin = 1;
    private static final int tmax = 26;
    private static final int skew = 38;
    private static final int damp = 700;
    private static final int initial_bias = 72;
    private static final int initial_n = 128;
    private static final char delimiter = '-';
    private static final String ACE_PREFIX = "xn--";

    private int adapt(final int delta, final int numpoints, final boolean firsttime) {
        int d = delta;
        if (firsttime) {
            d = d / damp;
        } else {
            d = d / 2;
        }
        d = d + (d / numpoints);
        int k = 0;
        while (d > ((base - tmin) * tmax) / 2) {
          d = d / (base - tmin);
          k = k + base;
        }
        return k + (((base - tmin + 1) * d) / (d + skew));
    }

    private int digit(final char c) {
        if ((c >= 'A') && (c <= 'Z')) {
            return (c - 'A');
        }
        if ((c >= 'a') && (c <= 'z')) {
            return (c - 'a');
        }
        if ((c >= '0') && (c <= '9')) {
            return (c - '0') + 26;
        }
        throw new IllegalArgumentException("illegal digit: "+ c);
    }

    @Override
    public String toUnicode(final String punycode) {
        final StringBuilder unicode = new StringBuilder(punycode.length());
        final StringTokenizer tok = new StringTokenizer(punycode, ".");
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            if (unicode.length() > 0) {
                unicode.append('.');
            }
            if (t.startsWith(ACE_PREFIX)) {
                t = decode(t.substring(4));
            }
            unicode.append(t);
        }
        return unicode.toString();
    }

    protected String decode(final String s) {
        String input = s;
        int n = initial_n;
        int i = 0;
        int bias = initial_bias;
        final StringBuilder output = new StringBuilder(input.length());
        final int lastdelim = input.lastIndexOf(delimiter);
        if (lastdelim != -1) {
            output.append(input.subSequence(0, lastdelim));
            input = input.substring(lastdelim + 1);
        }

        while (!input.isEmpty()) {
            final int oldi = i;
            int w = 1;
            for (int k = base;; k += base) {
                if (input.isEmpty()) {
                    break;
                }
                final char c = input.charAt(0);
                input = input.substring(1);
                final int digit = digit(c);
                i = i + digit * w; // FIXME fail on overflow
                final int t;
                if (k <= bias + tmin) {
                    t = tmin;
                } else if (k >= bias + tmax) {
                    t = tmax;
                } else {
                    t = k - bias;
                }
                if (digit < t) {
                    break;
                }
                w = w * (base - t); // FIXME fail on overflow
            }
            bias = adapt(i - oldi, output.length() + 1, (oldi == 0));
            n = n + i / (output.length() + 1); // FIXME fail on overflow
            i = i % (output.length() + 1);
            // {if n is a basic code point then fail}
            output.insert(i, (char) n);
            i++;
        }
        return output.toString();
    }

}
