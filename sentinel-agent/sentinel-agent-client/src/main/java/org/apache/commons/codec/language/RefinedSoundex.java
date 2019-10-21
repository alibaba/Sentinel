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

package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

/**
 * Encodes a string into a Refined Soundex value. A refined soundex code is
 * optimized for spell checking words. Soundex method originally developed by
 * <CITE>Margaret Odell</CITE> and <CITE>Robert Russell</CITE>.
 *
 * <p>This class is immutable and thread-safe.</p>
 *
 * @version $Id: RefinedSoundex.java 1429868 2013-01-07 16:08:05Z ggregory $
 */
public class RefinedSoundex implements StringEncoder {

    /**
     * @since 1.4
     */
    public static final String US_ENGLISH_MAPPING_STRING = "01360240043788015936020505";

   /**
     * RefinedSoundex is *refined* for a number of reasons one being that the
     * mappings have been altered. This implementation contains default
     * mappings for US English.
     */
    private static final char[] US_ENGLISH_MAPPING = US_ENGLISH_MAPPING_STRING.toCharArray();

    /**
     * Every letter of the alphabet is "mapped" to a numerical value. This char
     * array holds the values to which each letter is mapped. This
     * implementation contains a default map for US_ENGLISH
     */
    private final char[] soundexMapping;

    /**
     * This static variable contains an instance of the RefinedSoundex using
     * the US_ENGLISH mapping.
     */
    public static final RefinedSoundex US_ENGLISH = new RefinedSoundex();

     /**
     * Creates an instance of the RefinedSoundex object using the default US
     * English mapping.
     */
    public RefinedSoundex() {
        this.soundexMapping = US_ENGLISH_MAPPING;
    }

    /**
     * Creates a refined soundex instance using a custom mapping. This
     * constructor can be used to customize the mapping, and/or possibly
     * provide an internationalized mapping for a non-Western character set.
     *
     * @param mapping
     *                  Mapping array to use when finding the corresponding code for
     *                  a given character
     */
    public RefinedSoundex(final char[] mapping) {
        this.soundexMapping = new char[mapping.length];
        System.arraycopy(mapping, 0, this.soundexMapping, 0, mapping.length);
    }

    /**
     * Creates a refined Soundex instance using a custom mapping. This constructor can be used to customize the mapping,
     * and/or possibly provide an internationalized mapping for a non-Western character set.
     *
     * @param mapping
     *            Mapping string to use when finding the corresponding code for a given character
     * @since 1.4
     */
    public RefinedSoundex(final String mapping) {
        this.soundexMapping = mapping.toCharArray();
    }

    /**
     * Returns the number of characters in the two encoded Strings that are the
     * same. This return value ranges from 0 to the length of the shortest
     * encoded String: 0 indicates little or no similarity, and 4 out of 4 (for
     * example) indicates strong similarity or identical values. For refined
     * Soundex, the return value can be greater than 4.
     *
     * @param s1
     *                  A String that will be encoded and compared.
     * @param s2
     *                  A String that will be encoded and compared.
     * @return The number of characters in the two encoded Strings that are the
     *             same from 0 to to the length of the shortest encoded String.
     *
     * @see SoundexUtils#difference(StringEncoder,String,String)
     * @see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/tsqlref/ts_de-dz_8co5.asp">
     *          MS T-SQL DIFFERENCE</a>
     *
     * @throws EncoderException
     *                  if an error occurs encoding one of the strings
     * @since 1.3
     */
    public int difference(final String s1, final String s2) throws EncoderException {
        return SoundexUtils.difference(this, s1, s2);
    }

    /**
     * Encodes an Object using the refined soundex algorithm. This method is
     * provided in order to satisfy the requirements of the Encoder interface,
     * and will throw an EncoderException if the supplied object is not of type
     * java.lang.String.
     *
     * @param obj
     *                  Object to encode
     * @return An object (or type java.lang.String) containing the refined
     *             soundex code which corresponds to the String supplied.
     * @throws EncoderException
     *                  if the parameter supplied is not of type java.lang.String
     */
    @Override
    public Object encode(final Object obj) throws EncoderException {
        if (!(obj instanceof String)) {
            throw new EncoderException("Parameter supplied to RefinedSoundex encode is not of type java.lang.String");
        }
        return soundex((String) obj);
    }

    /**
     * Encodes a String using the refined soundex algorithm.
     *
     * @param str
     *                  A String object to encode
     * @return A Soundex code corresponding to the String supplied
     */
    @Override
    public String encode(final String str) {
        return soundex(str);
    }

    /**
     * Returns the mapping code for a given character. The mapping codes are
     * maintained in an internal char array named soundexMapping, and the
     * default values of these mappings are US English.
     *
     * @param c
     *                  char to get mapping for
     * @return A character (really a numeral) to return for the given char
     */
    char getMappingCode(final char c) {
        if (!Character.isLetter(c)) {
            return 0;
        }
        return this.soundexMapping[Character.toUpperCase(c) - 'A'];
    }

    /**
     * Retrieves the Refined Soundex code for a given String object.
     *
     * @param str
     *                  String to encode using the Refined Soundex algorithm
     * @return A soundex code for the String supplied
     */
    public String soundex(String str) {
        if (str == null) {
            return null;
        }
        str = SoundexUtils.clean(str);
        if (str.length() == 0) {
            return str;
        }

        final StringBuilder sBuf = new StringBuilder();
        sBuf.append(str.charAt(0));

        char last, current;
        last = '*';

        for (int i = 0; i < str.length(); i++) {

            current = getMappingCode(str.charAt(i));
            if (current == last) {
                continue;
            } else if (current != 0) {
                sBuf.append(current);
            }

            last = current;

        }

        return sBuf.toString();
    }
}
