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

import java.io.IOException;
import java.io.Reader;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.util.PublicSuffixList;

/**
 * Parses the list from <a href="http://publicsuffix.org/">publicsuffix.org</a>
 * and configures a PublicSuffixFilter.
 *
 * @deprecated (4.4) use {@link org.apache.http.conn.util.PublicSuffixListParser}.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public class PublicSuffixListParser {

    private final PublicSuffixFilter filter;
    private final org.apache.http.conn.util.PublicSuffixListParser parser;

    PublicSuffixListParser(final PublicSuffixFilter filter) {
        this.filter = filter;
        this.parser = new org.apache.http.conn.util.PublicSuffixListParser();
    }

    /**
     * Parses the public suffix list format.
     * When creating the reader from the file, make sure to
     * use the correct encoding (the original list is in UTF-8).
     *
     * @param reader the suffix list. The caller is responsible for closing the reader.
     * @throws IOException on error while reading from list
     */
    public void parse(final Reader reader) throws IOException {
        final PublicSuffixList suffixList = parser.parse(reader);
        filter.setPublicSuffixes(suffixList.getRules());
        filter.setExceptions(suffixList.getExceptions());
    }

}
