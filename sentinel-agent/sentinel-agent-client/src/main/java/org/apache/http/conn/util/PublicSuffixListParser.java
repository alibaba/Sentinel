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
package org.apache.http.conn.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;

/**
 * Parses the list from <a href="http://publicsuffix.org/">publicsuffix.org</a>
 * and configures a PublicSuffixFilter.
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class PublicSuffixListParser {

    public PublicSuffixListParser() {
    }

    /**
     * Parses the public suffix list format.
     * <p>
     * When creating the reader from the file, make sure to use the correct encoding
     * (the original list is in UTF-8).
     *
     * @param reader the data reader. The caller is responsible for closing the reader.
     * @throws java.io.IOException on error while reading from list
     */
    public PublicSuffixList parse(final Reader reader) throws IOException {
        final List<String> rules = new ArrayList<String>();
        final List<String> exceptions = new ArrayList<String>();
        final BufferedReader r = new BufferedReader(reader);

        String line;
        while ((line = r.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("//")) {
                continue; //entire lines can also be commented using //
            }
            if (line.startsWith(".")) {
                line = line.substring(1); // A leading dot is optional
            }
            // An exclamation mark (!) at the start of a rule marks an exception to a previous wildcard rule
            final boolean isException = line.startsWith("!");
            if (isException) {
                line = line.substring(1);
            }

            if (isException) {
                exceptions.add(line);
            } else {
                rules.add(line);
            }
        }
        return new PublicSuffixList(DomainType.UNKNOWN, rules, exceptions);
    }

    /**
     * Parses the public suffix list format by domain type (currently supported ICANN and PRIVATE).
     * <p>
     * When creating the reader from the file, make sure to use the correct encoding
     * (the original list is in UTF-8).
     *
     * @param reader the data reader. The caller is responsible for closing the reader.
     * @throws java.io.IOException on error while reading from list
     *
     * @since 4.5
     */
    public List<PublicSuffixList> parseByType(final Reader reader) throws IOException {
        final List<PublicSuffixList> result = new ArrayList<PublicSuffixList>(2);

        final BufferedReader r = new BufferedReader(reader);
        final StringBuilder sb = new StringBuilder(256);

        DomainType domainType = null;
        List<String> rules = null;
        List<String> exceptions = null;
        String line;
        while ((line = r.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("//")) {

                if (domainType == null) {
                    if (line.contains("===BEGIN ICANN DOMAINS===")) {
                        domainType = DomainType.ICANN;
                    } else if (line.contains("===BEGIN PRIVATE DOMAINS===")) {
                        domainType = DomainType.PRIVATE;
                    }
                } else {
                    if (line.contains("===END ICANN DOMAINS===") || line.contains("===END PRIVATE DOMAINS===")) {
                        if (rules != null) {
                            result.add(new PublicSuffixList(domainType, rules, exceptions));
                        }
                        domainType = null;
                        rules = null;
                        exceptions = null;
                    }
                }

                continue; //entire lines can also be commented using //
            }
            if (domainType == null) {
                continue;
            }

            if (line.startsWith(".")) {
                line = line.substring(1); // A leading dot is optional
            }
            // An exclamation mark (!) at the start of a rule marks an exception to a previous wildcard rule
            final boolean isException = line.startsWith("!");
            if (isException) {
                line = line.substring(1);
            }

            if (isException) {
                if (exceptions == null) {
                    exceptions = new ArrayList<String>();
                }
                exceptions.add(line);
            } else {
                if (rules == null) {
                    rules = new ArrayList<String>();
                }
                rules.add(line);
            }
        }
        return result;
    }

}
