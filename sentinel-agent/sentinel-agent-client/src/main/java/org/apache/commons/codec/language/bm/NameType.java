/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.codec.language.bm;

/**
 * Supported types of names. Unless you are matching particular family names, use {@link #GENERIC}. The
 * <code>GENERIC</code> NameType should work reasonably well for non-name words. The other encodings are
 * specifically tuned to family names, and may not work well at all for general text.
 *
 * @since 1.6
 * @version $Id: NameType.java 1429868 2013-01-07 16:08:05Z ggregory $
 */
public enum NameType {

    /** Ashkenazi family names */
    ASHKENAZI("ash"),

    /** Generic names and words */
    GENERIC("gen"),

    /** Sephardic family names */
    SEPHARDIC("sep");

    private final String name;

    NameType(final String name) {
        this.name = name;
    }

    /**
     * Gets the short version of the name type.
     *
     * @return the NameType short string
     */
    public String getName() {
        return this.name;
    }
}
