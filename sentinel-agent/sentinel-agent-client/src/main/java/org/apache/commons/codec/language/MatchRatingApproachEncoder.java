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

import java.util.Locale;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

/**
 * Match Rating Approach Phonetic Algorithm Developed by <CITE>Western Airlines</CITE> in 1977.
 *
 * This class is immutable and thread-safe.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Match_rating_approach">Wikipedia - Match Rating Approach</a>
 * @since 1.8
 */
public class MatchRatingApproachEncoder implements StringEncoder {

    private static final String SPACE = " ";

    private static final String EMPTY = "";

    /**
     * Constants used mainly for the min rating value.
     */
    private static final int ONE = 1, TWO = 2, THREE = 3, FOUR = 4, FIVE = 5, SIX = 6, SEVEN = 7, EIGHT = 8,
                             ELEVEN = 11, TWELVE = 12;

    /**
     * The plain letter equivalent of the accented letters.
     */
    private static final String PLAIN_ASCII = "AaEeIiOoUu" + // grave
            "AaEeIiOoUuYy" + // acute
            "AaEeIiOoUuYy" + // circumflex
            "AaOoNn" + // tilde
            "AaEeIiOoUuYy" + // umlaut
            "Aa" + // ring
            "Cc" + // cedilla
            "OoUu"; // double acute

    /**
     * Unicode characters corresponding to various accented letters. For example: \u00DA is U acute etc...
     */
    private static final String UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9" +
            "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD" +
            "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" +
            "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1" +
            "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" +
            "\u00C5\u00E5" + "\u00C7\u00E7" + "\u0150\u0151\u0170\u0171";

    private static final String[] DOUBLE_CONSONANT =
            new String[] { "BB", "CC", "DD", "FF", "GG", "HH", "JJ", "KK", "LL", "MM", "NN", "PP", "QQ", "RR", "SS",
                           "TT", "VV", "WW", "XX", "YY", "ZZ" };

    /**
     * Cleans up a name: 1. Upper-cases everything 2. Removes some common punctuation 3. Removes accents 4. Removes any
     * spaces.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            The name to be cleaned
     * @return The cleaned name
     */
    String cleanName(final String name) {
        String upperName = name.toUpperCase(Locale.ENGLISH);

        final String[] charsToTrim = { "\\-", "[&]", "\\'", "\\.", "[\\,]" };
        for (final String str : charsToTrim) {
            upperName = upperName.replaceAll(str, EMPTY);
        }

        upperName = removeAccents(upperName);
        upperName = upperName.replaceAll("\\s+", EMPTY);

        return upperName;
    }

    /**
     * Encodes an Object using the Match Rating Approach algorithm. Method is here to satisfy the requirements of the
     * Encoder interface Throws an EncoderException if input object is not of type java.lang.String.
     *
     * @param pObject
     *            Object to encode
     * @return An object (or type java.lang.String) containing the Match Rating Approach code which corresponds to the
     *         String supplied.
     * @throws EncoderException
     *             if the parameter supplied is not of type java.lang.String
     */
    @Override
    public final Object encode(final Object pObject) throws EncoderException {
        if (!(pObject instanceof String)) {
            throw new EncoderException(
                    "Parameter supplied to Match Rating Approach encoder is not of type java.lang.String");
        }
        return encode((String) pObject);
    }

    /**
     * Encodes a String using the Match Rating Approach (MRA) algorithm.
     *
     * @param name
     *            String object to encode
     * @return The MRA code corresponding to the String supplied
     */
    @Override
    public final String encode(String name) {
        // Bulletproof for trivial input - NINO
        if (name == null || EMPTY.equalsIgnoreCase(name) || SPACE.equalsIgnoreCase(name) || name.length() == 1) {
            return EMPTY;
        }

        // Preprocessing
        name = cleanName(name);

        // BEGIN: Actual encoding part of the algorithm...
        // 1. Delete all vowels unless the vowel begins the word
        name = removeVowels(name);

        // 2. Remove second consonant from any double consonant
        name = removeDoubleConsonants(name);

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name);

        return name;
    }

    /**
     * Gets the first & last 3 letters of a name (if > 6 characters) Else just returns the name.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            The string to get the substrings from
     * @return Annexed first & last 3 letters of input word.
     */
    String getFirst3Last3(final String name) {
        final int nameLength = name.length();

        if (nameLength > SIX) {
            final String firstThree = name.substring(0, THREE);
            final String lastThree = name.substring(nameLength - THREE, nameLength);
            return firstThree + lastThree;
        } else {
            return name;
        }
    }

    /**
     * Obtains the min rating of the length sum of the 2 names. In essence the larger the sum length the smaller the
     * min rating. Values strictly from documentation.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param sumLength
     *            The length of 2 strings sent down
     * @return The min rating value
     */
    int getMinRating(final int sumLength) {
        int minRating = 0;

        if (sumLength <= FOUR) {
            minRating = FIVE;
        } else if (sumLength >= FIVE && sumLength <= SEVEN) {
            minRating = FOUR;
        } else if (sumLength >= EIGHT && sumLength <= ELEVEN) {
            minRating = THREE;
        } else if (sumLength == TWELVE) {
            minRating = TWO;
        } else {
            minRating = ONE; // docs said little here.
        }

        return minRating;
    }

    /**
     * Determines if two names are homophonous via Match Rating Approach (MRA) algorithm. It should be noted that the
     * strings are cleaned in the same way as {@link #encode(String)}.
     *
     * @param name1
     *            First of the 2 strings (names) to compare
     * @param name2
     *            Second of the 2 names to compare
     * @return <code>true</code> if the encodings are identical <code>false</code> otherwise.
     */
    public boolean isEncodeEquals(String name1, String name2) {
        // Bulletproof for trivial input - NINO
        if (name1 == null || EMPTY.equalsIgnoreCase(name1) || SPACE.equalsIgnoreCase(name1)) {
            return false;
        } else if (name2 == null || EMPTY.equalsIgnoreCase(name2) || SPACE.equalsIgnoreCase(name2)) {
            return false;
        } else if (name1.length() == 1 || name2.length() == 1) {
            return false;
        } else if (name1.equalsIgnoreCase(name2)) {
            return true;
        }

        // Preprocessing
        name1 = cleanName(name1);
        name2 = cleanName(name2);

        // Actual MRA Algorithm

        // 1. Remove vowels
        name1 = removeVowels(name1);
        name2 = removeVowels(name2);

        // 2. Remove double consonants
        name1 = removeDoubleConsonants(name1);
        name2 = removeDoubleConsonants(name2);

        // 3. Reduce down to 3 letters
        name1 = getFirst3Last3(name1);
        name2 = getFirst3Last3(name2);

        // 4. Check for length difference - if 3 or greater then no similarity
        // comparison is done
        if (Math.abs(name1.length() - name2.length()) >= THREE) {
            return false;
        }

        // 5. Obtain the minimum rating value by calculating the length sum of the
        // encoded Strings and sending it down.
        final int sumLength = Math.abs(name1.length() + name2.length());
        int minRating = 0;
        minRating = getMinRating(sumLength);

        // 6. Process the encoded Strings from left to right and remove any
        // identical characters found from both Strings respectively.
        final int count = leftToRightThenRightToLeftProcessing(name1, name2);

        // 7. Each PNI item that has a similarity rating equal to or greater than
        // the min is considered to be a good candidate match
        return count >= minRating;

    }

    /**
     * Determines if a letter is a vowel.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param letter
     *            The letter under investiagtion
     * @return True if a vowel, else false
     */
    boolean isVowel(final String letter) {
        return letter.equalsIgnoreCase("E") || letter.equalsIgnoreCase("A") || letter.equalsIgnoreCase("O") ||
               letter.equalsIgnoreCase("I") || letter.equalsIgnoreCase("U");
    }

    /**
     * Processes the names from left to right (first) then right to left removing identical letters in same positions.
     * Then subtracts the longer string that remains from 6 and returns this.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name1
     *            name2
     * @return
     */
    int leftToRightThenRightToLeftProcessing(final String name1, final String name2) {
        final char[] name1Char = name1.toCharArray();
        final char[] name2Char = name2.toCharArray();

        final int name1Size = name1.length() - 1;
        final int name2Size = name2.length() - 1;

        String name1LtRStart = EMPTY;
        String name1LtREnd = EMPTY;

        String name2RtLStart = EMPTY;
        String name2RtLEnd = EMPTY;

        for (int i = 0; i < name1Char.length; i++) {
            if (i > name2Size) {
                break;
            }

            name1LtRStart = name1.substring(i, i + 1);
            name1LtREnd = name1.substring(name1Size - i, name1Size - i + 1);

            name2RtLStart = name2.substring(i, i + 1);
            name2RtLEnd = name2.substring(name2Size - i, name2Size - i + 1);

            // Left to right...
            if (name1LtRStart.equals(name2RtLStart)) {
                name1Char[i] = ' ';
                name2Char[i] = ' ';
            }

            // Right to left...
            if (name1LtREnd.equals(name2RtLEnd)) {
                name1Char[name1Size - i] = ' ';
                name2Char[name2Size - i] = ' ';
            }
        }

        // Char arrays -> string & remove extraneous space
        final String strA = new String(name1Char).replaceAll("\\s+", EMPTY);
        final String strB = new String(name2Char).replaceAll("\\s+", EMPTY);

        // Final bit - subtract longest string from 6 and return this int value
        if (strA.length() > strB.length()) {
            return Math.abs(SIX - strA.length());
        } else {
            return Math.abs(SIX - strB.length());
        }
    }

    /**
     * Removes accented letters and replaces with non-accented ascii equivalent Case is preserved.
     * http://www.codecodex.com/wiki/Remove_accent_from_letters_%28ex_.%C3%A9_to_e%29
     *
     * @param accentedWord
     *            The word that may have accents in it.
     * @return De-accented word
     */
    String removeAccents(final String accentedWord) {
        if (accentedWord == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        final int n = accentedWord.length();

        for (int i = 0; i < n; i++) {
            final char c = accentedWord.charAt(i);
            final int pos = UNICODE.indexOf(c);
            if (pos > -1) {
                sb.append(PLAIN_ASCII.charAt(pos));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Replaces any double consonant pair with the single letter equivalent.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            String to have double consonants removed
     * @return Single consonant word
     */
    String removeDoubleConsonants(final String name) {
        String replacedName = name.toUpperCase();
        for (final String dc : DOUBLE_CONSONANT) {
            if (replacedName.contains(dc)) {
                final String singleLetter = dc.substring(0, 1);
                replacedName = replacedName.replace(dc, singleLetter);
            }
        }
        return replacedName;
    }

    /**
     * Deletes all vowels unless the vowel begins the word.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            The name to have vowels removed
     * @return De-voweled word
     */
    String removeVowels(String name) {
        // Extract first letter
        final String firstLetter = name.substring(0, 1);

        name = name.replaceAll("A", EMPTY);
        name = name.replaceAll("E", EMPTY);
        name = name.replaceAll("I", EMPTY);
        name = name.replaceAll("O", EMPTY);
        name = name.replaceAll("U", EMPTY);

        name = name.replaceAll("\\s{2,}\\b", SPACE);

        // return isVowel(firstLetter) ? (firstLetter + name) : name;
        if (isVowel(firstLetter)) {
            return firstLetter + name;
        } else {
            return name;
        }
    }
}
