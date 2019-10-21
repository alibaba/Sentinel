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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.util.Args;

/**
 * {@link org.apache.http.conn.util.PublicSuffixMatcher} loader.
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.SAFE)
public final class PublicSuffixMatcherLoader {

    private static PublicSuffixMatcher load(final InputStream in) throws IOException {
        final List<PublicSuffixList> lists = new PublicSuffixListParser().parseByType(
                new InputStreamReader(in, Consts.UTF_8));
        return new PublicSuffixMatcher(lists);
    }

    public static PublicSuffixMatcher load(final URL url) throws IOException {
        Args.notNull(url, "URL");
        final InputStream in = url.openStream();
        try {
            return load(in);
        } finally {
            in.close();
        }
    }

    public static PublicSuffixMatcher load(final File file) throws IOException {
        Args.notNull(file, "File");
        final InputStream in = new FileInputStream(file);
        try {
            return load(in);
        } finally {
            in.close();
        }
    }

    private static volatile PublicSuffixMatcher DEFAULT_INSTANCE;

    public static PublicSuffixMatcher getDefault() {
        if (DEFAULT_INSTANCE == null) {
            synchronized (PublicSuffixMatcherLoader.class) {
                if (DEFAULT_INSTANCE == null){
                    final URL url = PublicSuffixMatcherLoader.class.getResource(
                            "/mozilla/public-suffix-list.txt");
                    if (url != null) {
                        try {
                            DEFAULT_INSTANCE = load(url);
                        } catch (final IOException ex) {
                            // Should never happen
                            final Log log = LogFactory.getLog(PublicSuffixMatcherLoader.class);
                            if (log.isWarnEnabled()) {
                                log.warn("Failure loading public suffix list from default resource", ex);
                            }
                        }
                    } else {
                        DEFAULT_INSTANCE = new PublicSuffixMatcher(Arrays.asList("com"), null);
                    }
                }
            }
        }
        return DEFAULT_INSTANCE;
    }

}
