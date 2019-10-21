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
 * Encodes a string into a Metaphone value.
 * <p>
 * Initial Java implementation by <CITE>William B. Brogden. December, 1997</CITE>.
 * Permission given by <CITE>wbrogden</CITE> for code to be used anywhere.
 * <p>
 * <CITE>Hanging on the Metaphone</CITE> by <CITE>Lawrence Philips</CITE> in <CITE>Computer Language of Dec. 1990,
 * p 39.</CITE>
 * <p>
 * Note, that this does not match the algorithm that ships with PHP, or the algorithm found in the Perl implementations:
 * </p>
 * <ul>
 * <li><a href="http://search.cpan.org/~mschwern/Text-Metaphone-1.96/Metaphone.pm">Text:Metaphone-1.96</a>
 *  (broken link 4/30/2013) </li>
 * <li><a href="https://metacpan.org/source/MSCHWERN/Text-Metaphone-1.96//Metaphone.pm">Text:Metaphone-1.96</a>
 *  (link checked 4/30/2013) </li>
 * </ul>
 * <p>
 * They have had undocumented changes from the originally published algorithm.
 * For more information, see <a href="https://issues.apache.org/jira/browse/CODEC-57">CODEC-57</a>.
 * <p>
 * This class is conditionally thread-safe.
 * The instance field {@link #maxCodeLen} is mutable {@link #setMaxCodeLen(int)}
 * but is not volatile, and accesses are not synchronized.
 * If an instance of the class is shared between threads, the caller needs to ensure that suitable synchronization
 * is used to ensure safe publication of the value between threads, and must not invoke {@link #setMaxCodeLen(int)}
 * after initial setup.
 *
 * @version $Id: Metaphone.java 1542813 2013-11-17 20:52:32Z tn $
 */
public class Metaphone implements StringEncoder {

    /**
     * Five values in the English language
     */
    private static final String VOWELS = "AEIOU";

    /**
     * Variable used in Metaphone algorithm
     */
    private static final String FRONTV = "EIY";

    /**
     * Variable used in Metaphone algorithm
     */
    private static final String VARSON = "CSPTG";

    /**
     * The max code length for metaphone is 4
     */
    private int maxCodeLen = 4;

    /**
     * Creates an instance of the Metaphone encoder
     */
    public Metaphone() {
        super();
    }

    /**
     * Find the metaphone value of a String. This is similar to the
     * soundex algorithm, but better at finding similar sounding words.
     * All input is converted to upper case.
     * Limitations: Input format is expected to be a single ASCII word
     * with only characters in the A - Z range, no punctuation or numbers.
     *
     * @param txt String to find the metaphone code for
     * @return A metaphone code corresponding to the String supplied
     */
    public String metaphone(final String txt) {
        boolean hard = false;
        if (txt == null || txt.length() == 0) {
            return "";
        }
        // single character is itself
        if (txt.length() == 1) {
            return txt.toUpperCase(java.util.Locale.ENGLISH);
        }

        final char[] inwd = txt.toUpperCase(java.util.Locale.ENGLISH).toCharArray();

        final StringBuilder local = new StringBuilder(40); // manipulate
        final StringBuilder code = new StringBuilder(10); //   output
        // handle initial 2 characters exceptions
        switch(inwd[0]) {
        case 'K':
        case 'G':
        case 'P': /* looking for KN, etc*/
            if (inwd[1] == 'N') {
                local.append(inwd, 1, inwd.length - 1);
            } else {
                local.append(inwd);
            }
            break;
        case 'A': /* looking for AE */
            if (inwd[1] == 'E') {
                local.append(inwd, 1, inwd.length - 1);
            } else {
                local.append(inwd);
            }
            break;
        case 'W': /* looking for WR or WH */
            if (inwd[1] == 'R') {   // WR -> R
                local.append(inwd, 1, inwd.length - 1);
                break;
            }
            if (inwd[1] == 'H') {
                local.append(inwd, 1, inwd.length - 1);
                local.setCharAt(0, 'W'); // WH -> W
            } else {
                local.append(inwd);
            }
            break;
        case 'X': /* initial X becomes S */
            inwd[0] = 'S';
            local.append(inwd);
            break;
        default:
            local.append(inwd);
        } // now local has working string with initials fixed

        final int wdsz = local.length();
        int n = 0;

        while (code.length() < this.getMaxCodeLen() &&
               n < wdsz ) { // max code size of 4 works well
            final char symb = local.charAt(n);
            // remove duplicate letters except C
            if (symb != 'C' && isPreviousChar( local, n, symb ) ) {
                n++;
            } else { // not dup
                switch(symb) {
                case 'A':
                case 'E':
                case 'I':
                case 'O':
                case 'U':
                    if (n == 0) {
                        code.append(symb);
                    }
                    break; // only use vowel if leading char
                case 'B':
                    if ( isPreviousChar(local, n, 'M') &&
                         isLastChar(wdsz, n) ) { // B is silent if word ends in MB
                        break;
                    }
                    code.append(symb);
                    break;
                case 'C': // lots of C special cases
                    /* discard if SCI, SCE or SCY */
                    if ( isPreviousChar(local, n, 'S') &&
                         !isLastChar(wdsz, n) &&
                         FRONTV.indexOf(local.charAt(n + 1)) >= 0 ) {
                        break;
                    }
                    if (regionMatch(local, n, "CIA")) { // "CIA" -> X
                        code.append('X');
                        break;
                    }
                    if (!isLastChar(wdsz, n) &&
                        FRONTV.indexOf(local.charAt(n + 1)) >= 0) {
                        code.append('S');
                        break; // CI,CE,CY -> S
                    }
                    if (isPreviousChar(local, n, 'S') &&
                        isNextChar(local, n, 'H') ) { // SCH->sk
                        code.append('K');
                        break;
                    }
                    if (isNextChar(local, n, 'H')) { // detect CH
                        if (n == 0 &&
                            wdsz >= 3 &&
                            isVowel(local,2) ) { // CH consonant -> K consonant
                            code.append('K');
                        } else {
                            code.append('X'); // CHvowel -> X
                        }
                    } else {
                        code.append('K');
                    }
                    break;
                case 'D':
                    if (!isLastChar(wdsz, n + 1) &&
                        isNextChar(local, n, 'G') &&
                        FRONTV.indexOf(local.charAt(n + 2)) >= 0) { // DGE DGI DGY -> J
                        code.append('J'); n += 2;
                    } else {
                        code.append('T');
                    }
                    break;
                case 'G': // GH silent at end or before consonant
                    if (isLastChar(wdsz, n + 1) &&
                        isNextChar(local, n, 'H')) {
                        break;
                    }
                    if (!isLastChar(wdsz, n + 1) &&
                        isNextChar(local,n,'H') &&
                        !isVowel(local,n+2)) {
                        break;
                    }
                    if (n > 0 &&
                        ( regionMatch(local, n, "GN") ||
                          regionMatch(local, n, "GNED") ) ) {
                        break; // silent G
                    }
                    if (isPreviousChar(local, n, 'G')) {
                        // NOTE: Given that duplicated chars are removed, I don't see how this can ever be true
                        hard = true;
                    } else {
                        hard = false;
                    }
                    if (!isLastChar(wdsz, n) &&
                        FRONTV.indexOf(local.charAt(n + 1)) >= 0 &&
                        !hard) {
                        code.append('J');
                    } else {
                        code.append('K');
                    }
                    break;
                case 'H':
                    if (isLastChar(wdsz, n)) {
                        break; // terminal H
                    }
                    if (n > 0 &&
                        VARSON.indexOf(local.charAt(n - 1)) >= 0) {
                        break;
                    }
                    if (isVowel(local,n+1)) {
                        code.append('H'); // Hvowel
                    }
                    break;
                case 'F':
                case 'J':
                case 'L':
                case 'M':
                case 'N':
                case 'R':
                    code.append(symb);
                    break;
                case 'K':
                    if (n > 0) { // not initial
                        if (!isPreviousChar(local, n, 'C')) {
                            code.append(symb);
                        }
                    } else {
                        code.append(symb); // initial K
                    }
                    break;
                case 'P':
                    if (isNextChar(local,n,'H')) {
                        // PH -> F
                        code.append('F');
                    } else {
                        code.append(symb);
                    }
                    break;
                case 'Q':
                    code.append('K');
                    break;
                case 'S':
                    if (regionMatch(local,n,"SH") ||
                        regionMatch(local,n,"SIO") ||
                        regionMatch(local,n,"SIA")) {
                        code.append('X');
                    } else {
                        code.append('S');
                    }
                    break;
                case 'T':
                    if (regionMatch(local,n,"TIA") ||
                        regionMatch(local,n,"TIO")) {
                        code.append('X');
                        break;
                    }
                    if (regionMatch(local,n,"TCH")) {
                        // Silent if in "TCH"
                        break;
                    }
                    // substitute numeral 0 for TH (resembles theta after all)
                    if (regionMatch(local,n,"TH")) {
                        code.append('0');
                    } else {
                        code.append('T');
                    }
                    break;
                case 'V':
                    code.append('F'); break;
                case 'W':
                case 'Y': // silent if not followed by vowel
                    if (!isLastChar(wdsz,n) &&
                        isVowel(local,n+1)) {
                        code.append(symb);
                    }
                    break;
                case 'X':
                    code.append('K');
                    code.append('S');
                    break;
                case 'Z':
                    code.append('S');
                    break;
                default:
                    // do nothing
                    break;
                } // end switch
                n++;
            } // end else from symb != 'C'
            if (code.length() > this.getMaxCodeLen()) {
                code.setLength(this.getMaxCodeLen());
            }
        }
        return code.toString();
    }

    private boolean isVowel(final StringBuilder string, final int index) {
        return VOWELS.indexOf(string.charAt(index)) >= 0;
    }

    private boolean isPreviousChar(final StringBuilder string, final int index, final char c) {
        boolean matches = false;
        if( index > 0 &&
            index < string.length() ) {
            matches = string.charAt(index - 1) == c;
        }
        return matches;
    }

    private boolean isNextChar(final StringBuilder string, final int index, final char c) {
        boolean matches = false;
        if( index >= 0 &&
            index < string.length() - 1 ) {
            matches = string.charAt(index + 1) == c;
        }
        return matches;
    }

    private boolean regionMatch(final StringBuilder string, final int index, final String test) {
        boolean matches = false;
        if( index >= 0 &&
            index + test.length() - 1 < string.length() ) {
            final String substring = string.substring( index, index + test.length());
            matches = substring.equals( test );
        }
        return matches;
    }

    private boolean isLastChar(final int wdsz, final int n) {
        return n + 1 == wdsz;
    }


    /**
     * Encodes an Object using the metaphone algorithm.  This method
     * is provided in order to satisfy the requirements of the
     * Encoder interface, and will throw an EncoderException if the
     * supplied object is not of type java.lang.String.
     *
     * @param obj Object to encode
     * @return An object (or type java.lang.String) containing the
     *         metaphone code which corresponds to the String supplied.
     * @throws EncoderException if the parameter supplied is not
     *                          of type java.lang.String
     */
    @Override
    public Object encode(final Object obj) throws EncoderException {
        if (!(obj instanceof String)) {
            throw new EncoderException("Parameter supplied to Metaphone encode is not of type java.lang.String");
        }
        return metaphone((String) obj);
    }

    /**
     * Encodes a String using the Metaphone algorithm.
     *
     * @param str String object to encode
     * @return The metaphone code corresponding to the String supplied
     */
    @Override
    public String encode(final String str) {
        return metaphone(str);
    }

    /**
     * Tests is the metaphones of two strings are identical.
     *
     * @param str1 First of two strings to compare
     * @param str2 Second of two strings to compare
     * @return {@code true} if the metaphones of these strings are identical,
     *        {@code false} otherwise.
     */
    public boolean isMetaphoneEqual(final String str1, final String str2) {
        return metaphone(str1).equals(metaphone(str2));
    }

    /**
     * Returns the maxCodeLen.
     * @return int
     */
    public int getMaxCodeLen() { return this.maxCodeLen; }

    /**
     * Sets the maxCodeLen.
     * @param maxCodeLen The maxCodeLen to set
     */
    public void setMaxCodeLen(final int maxCodeLen) { this.maxCodeLen = maxCodeLen; }

}
