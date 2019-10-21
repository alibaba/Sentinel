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

package org.apache.commons.codec;

import java.util.Comparator;

/**
 * Compares Strings using a {@link StringEncoder}. This comparator is used to sort Strings by an encoding scheme such as
 * Soundex, Metaphone, etc. This class can come in handy if one need to sort Strings by an encoded form of a name such
 * as Soundex.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @version $Id: StringEncoderComparator.java 1468177 2013-04-15 18:35:15Z ggregory $
 */
@SuppressWarnings("rawtypes")
// TODO ought to implement Comparator<String> but that's not possible whilst maintaining binary compatibility.
public class StringEncoderComparator implements Comparator {

    /**
     * Internal encoder instance.
     */
    private final StringEncoder stringEncoder;

    /**
     * Constructs a new instance.
     *
     * @deprecated Creating an instance without a {@link StringEncoder} leads to a {@link NullPointerException}. Will be
     *             removed in 2.0.
     */
    @Deprecated
    public StringEncoderComparator() {
        this.stringEncoder = null; // Trying to use this will cause things to break
    }

    /**
     * Constructs a new instance with the given algorithm.
     *
     * @param stringEncoder
     *            the StringEncoder used for comparisons.
     */
    public StringEncoderComparator(final StringEncoder stringEncoder) {
        this.stringEncoder = stringEncoder;
    }

    /**
     * Compares two strings based not on the strings themselves, but on an encoding of the two strings using the
     * StringEncoder this Comparator was created with.
     *
     * If an {@link EncoderException} is encountered, return <code>0</code>.
     *
     * @param o1
     *            the object to compare
     * @param o2
     *            the object to compare to
     * @return the Comparable.compareTo() return code or 0 if an encoding error was caught.
     * @see Comparable
     */
    @Override
    public int compare(final Object o1, final Object o2) {

        int compareCode = 0;

        try {
            @SuppressWarnings("unchecked") // May fail with CCE if encode returns something that is not Comparable
            // However this was always the case.
            final Comparable<Comparable<?>> s1 = (Comparable<Comparable<?>>) this.stringEncoder.encode(o1);
            final Comparable<?> s2 = (Comparable<?>) this.stringEncoder.encode(o2);
            compareCode = s1.compareTo(s2);
        } catch (final EncoderException ee) {
            compareCode = 0;
        }
        return compareCode;
    }

}
