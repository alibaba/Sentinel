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
 * Encodes a string into a Cologne Phonetic value.
 * <p>
 * Implements the <a href="http://de.wikipedia.org/wiki/K%C3%B6lner_Phonetik">K&ouml;lner Phonetik</a>
 * (Cologne Phonetic) algorithm issued by Hans Joachim Postel in 1969.
 * <p>
 * The <i>K&ouml;lner Phonetik</i> is a phonetic algorithm which is optimized for the German language.
 * It is related to the well-known soundex algorithm.
 * <p>
 *
 * <h2>Algorithm</h2>
 *
 * <ul>
 *
 * <li>
 * <h3>Step 1:</h3>
 * After preprocessing (conversion to upper case, transcription of <a
 * href="http://en.wikipedia.org/wiki/Germanic_umlaut">germanic umlauts</a>, removal of non alphabetical characters) the
 * letters of the supplied text are replaced by their phonetic code according to the following table.
 * <table border="1">
 * <tbody>
 * <tr>
 * <th>Letter</th>
 * <th>Context</th>
 * <th align="center">Code</th>
 * </tr>
 * <tr>
 * <td>A, E, I, J, O, U, Y</td>
 * <td></td>
 * <td align="center">0</td>
 * </tr>
 * <tr>
 *
 * <td>H</td>
 * <td></td>
 * <td align="center">-</td>
 * </tr>
 * <tr>
 * <td>B</td>
 * <td></td>
 * <td rowspan="2" align="center">1</td>
 * </tr>
 * <tr>
 * <td>P</td>
 * <td>not before H</td>
 *
 * </tr>
 * <tr>
 * <td>D, T</td>
 * <td>not before C, S, Z</td>
 * <td align="center">2</td>
 * </tr>
 * <tr>
 * <td>F, V, W</td>
 * <td></td>
 * <td rowspan="2" align="center">3</td>
 * </tr>
 * <tr>
 *
 * <td>P</td>
 * <td>before H</td>
 * </tr>
 * <tr>
 * <td>G, K, Q</td>
 * <td></td>
 * <td rowspan="3" align="center">4</td>
 * </tr>
 * <tr>
 * <td rowspan="2">C</td>
 * <td>at onset before A, H, K, L, O, Q, R, U, X</td>
 *
 * </tr>
 * <tr>
 * <td>before A, H, K, O, Q, U, X except after S, Z</td>
 * </tr>
 * <tr>
 * <td>X</td>
 * <td>not after C, K, Q</td>
 * <td align="center">48</td>
 * </tr>
 * <tr>
 * <td>L</td>
 * <td></td>
 *
 * <td align="center">5</td>
 * </tr>
 * <tr>
 * <td>M, N</td>
 * <td></td>
 * <td align="center">6</td>
 * </tr>
 * <tr>
 * <td>R</td>
 * <td></td>
 * <td align="center">7</td>
 * </tr>
 *
 * <tr>
 * <td>S, Z</td>
 * <td></td>
 * <td rowspan="6" align="center">8</td>
 * </tr>
 * <tr>
 * <td rowspan="3">C</td>
 * <td>after S, Z</td>
 * </tr>
 * <tr>
 * <td>at onset except before A, H, K, L, O, Q, R, U, X</td>
 * </tr>
 *
 * <tr>
 * <td>not before A, H, K, O, Q, U, X</td>
 * </tr>
 * <tr>
 * <td>D, T</td>
 * <td>before C, S, Z</td>
 * </tr>
 * <tr>
 * <td>X</td>
 * <td>after C, K, Q</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * <small><i>(Source: <a href= "http://de.wikipedia.org/wiki/K%C3%B6lner_Phonetik#Buchstabencodes" >Wikipedia (de):
 * K&ouml;lner Phonetik -- Buchstabencodes</a>)</i></small>
 * </p>
 *
 * <h4>Example:</h4>
 *
 * {@code "M}&uuml;{@code ller-L}&uuml;{@code denscheidt" => "MULLERLUDENSCHEIDT" => "6005507500206880022"}
 *
 * </li>
 *
 * <li>
 * <h3>Step 2:</h3>
 * Collapse of all multiple consecutive code digits.
 * <h4>Example:</h4>
 * {@code "6005507500206880022" => "6050750206802"}</li>
 *
 * <li>
 * <h3>Step 3:</h3>
 * Removal of all codes "0" except at the beginning. This means that two or more identical consecutive digits can occur
 * if they occur after removing the "0" digits.
 *
 * <h4>Example:</h4>
 * {@code "6050750206802" => "65752682"}</li>
 *
 * </ul>
 *
 * This class is thread-safe.
 *
 * @see <a href="http://de.wikipedia.org/wiki/K%C3%B6lner_Phonetik">Wikipedia (de): K&ouml;lner Phonetik (in German)</a>
 * @since 1.5
 */
public class ColognePhonetic implements StringEncoder {

    // Predefined char arrays for better performance and less GC load
    private static final char[] AEIJOUY = new char[] { 'A', 'E', 'I', 'J', 'O', 'U', 'Y' };
    private static final char[] SCZ = new char[] { 'S', 'C', 'Z' };
    private static final char[] WFPV = new char[] { 'W', 'F', 'P', 'V' };
    private static final char[] GKQ = new char[] { 'G', 'K', 'Q' };
    private static final char[] CKQ = new char[] { 'C', 'K', 'Q' };
    private static final char[] AHKLOQRUX = new char[] { 'A', 'H', 'K', 'L', 'O', 'Q', 'R', 'U', 'X' };
    private static final char[] SZ = new char[] { 'S', 'Z' };
    private static final char[] AHOUKQX = new char[] { 'A', 'H', 'O', 'U', 'K', 'Q', 'X' };
    private static final char[] TDX = new char[] { 'T', 'D', 'X' };

    /**
     * This class is not thread-safe; the field {@link #length} is mutable.
     * However, it is not shared between threads, as it is constructed on demand
     * by the method {@link ColognePhonetic#colognePhonetic(String)}
     */
    private abstract class CologneBuffer {

        protected final char[] data;

        protected int length = 0;

        public CologneBuffer(final char[] data) {
            this.data = data;
            this.length = data.length;
        }

        public CologneBuffer(final int buffSize) {
            this.data = new char[buffSize];
            this.length = 0;
        }

        protected abstract char[] copyData(int start, final int length);

        public int length() {
            return length;
        }

        @Override
        public String toString() {
            return new String(copyData(0, length));
        }
    }

    private class CologneOutputBuffer extends CologneBuffer {

        public CologneOutputBuffer(final int buffSize) {
            super(buffSize);
        }

        public void addRight(final char chr) {
            data[length] = chr;
            length++;
        }

        @Override
        protected char[] copyData(final int start, final int length) {
            final char[] newData = new char[length];
            System.arraycopy(data, start, newData, 0, length);
            return newData;
        }
    }

    private class CologneInputBuffer extends CologneBuffer {

        public CologneInputBuffer(final char[] data) {
            super(data);
        }

        public void addLeft(final char ch) {
            length++;
            data[getNextPos()] = ch;
        }

        @Override
        protected char[] copyData(final int start, final int length) {
            final char[] newData = new char[length];
            System.arraycopy(data, data.length - this.length + start, newData, 0, length);
            return newData;
        }

        public char getNextChar() {
            return data[getNextPos()];
        }

        protected int getNextPos() {
            return data.length - length;
        }

        public char removeNext() {
            final char ch = getNextChar();
            length--;
            return ch;
        }
    }

    /**
     * Maps some Germanic characters to plain for internal processing. The following characters are mapped:
     * <ul>
     * <li>capital a, umlaut mark</li>
     * <li>capital u, umlaut mark</li>
     * <li>capital o, umlaut mark</li>
     * <li>small sharp s, German</li>
     * </ul>
     */
    private static final char[][] PREPROCESS_MAP = new char[][]{
        {'\u00C4', 'A'}, // capital a, umlaut mark
        {'\u00DC', 'U'}, // capital u, umlaut mark
        {'\u00D6', 'O'}, // capital o, umlaut mark
        {'\u00DF', 'S'} // small sharp s, German
    };

    /*
     * Returns whether the array contains the key, or not.
     */
    private static boolean arrayContains(final char[] arr, final char key) {
        for (final char element : arr) {
            if (element == key) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Implements the <i>K&ouml;lner Phonetik</i> algorithm.
     * </p>
     * <p>
     * In contrast to the initial description of the algorithm, this implementation does the encoding in one pass.
     * </p>
     *
     * @param text
     * @return the corresponding encoding according to the <i>K&ouml;lner Phonetik</i> algorithm
     */
    public String colognePhonetic(String text) {
        if (text == null) {
            return null;
        }

        text = preprocess(text);

        final CologneOutputBuffer output = new CologneOutputBuffer(text.length() * 2);
        final CologneInputBuffer input = new CologneInputBuffer(text.toCharArray());

        char nextChar;

        char lastChar = '-';
        char lastCode = '/';
        char code;
        char chr;

        int rightLength = input.length();

        while (rightLength > 0) {
            chr = input.removeNext();

            if ((rightLength = input.length()) > 0) {
                nextChar = input.getNextChar();
            } else {
                nextChar = '-';
            }

            if (arrayContains(AEIJOUY, chr)) {
                code = '0';
            } else if (chr == 'H' || chr < 'A' || chr > 'Z') {
                if (lastCode == '/') {
                    continue;
                }
                code = '-';
            } else if (chr == 'B' || (chr == 'P' && nextChar != 'H')) {
                code = '1';
            } else if ((chr == 'D' || chr == 'T') && !arrayContains(SCZ, nextChar)) {
                code = '2';
            } else if (arrayContains(WFPV, chr)) {
                code = '3';
            } else if (arrayContains(GKQ, chr)) {
                code = '4';
            } else if (chr == 'X' && !arrayContains(CKQ, lastChar)) {
                code = '4';
                input.addLeft('S');
                rightLength++;
            } else if (chr == 'S' || chr == 'Z') {
                code = '8';
            } else if (chr == 'C') {
                if (lastCode == '/') {
                    if (arrayContains(AHKLOQRUX, nextChar)) {
                        code = '4';
                    } else {
                        code = '8';
                    }
                } else {
                    if (arrayContains(SZ, lastChar) || !arrayContains(AHOUKQX, nextChar)) {
                        code = '8';
                    } else {
                        code = '4';
                    }
                }
            } else if (arrayContains(TDX, chr)) {
                code = '8';
            } else if (chr == 'R') {
                code = '7';
            } else if (chr == 'L') {
                code = '5';
            } else if (chr == 'M' || chr == 'N') {
                code = '6';
            } else {
                code = chr;
            }

            if (code != '-' && (lastCode != code && (code != '0' || lastCode == '/') || code < '0' || code > '8')) {
                output.addRight(code);
            }

            lastChar = chr;
            lastCode = code;
        }
        return output.toString();
    }

    @Override
    public Object encode(final Object object) throws EncoderException {
        if (!(object instanceof String)) {
            throw new EncoderException("This method's parameter was expected to be of the type " +
                String.class.getName() +
                ". But actually it was of the type " +
                object.getClass().getName() +
                ".");
        }
        return encode((String) object);
    }

    @Override
    public String encode(final String text) {
        return colognePhonetic(text);
    }

    public boolean isEncodeEqual(final String text1, final String text2) {
        return colognePhonetic(text1).equals(colognePhonetic(text2));
    }

    /**
     * Converts the string to upper case and replaces germanic characters as defined in {@link #PREPROCESS_MAP}.
     */
    private String preprocess(String text) {
        text = text.toUpperCase(Locale.GERMAN);

        final char[] chrs = text.toCharArray();

        for (int index = 0; index < chrs.length; index++) {
            if (chrs[index] > 'Z') {
                for (final char[] element : PREPROCESS_MAP) {
                    if (chrs[index] == element[0]) {
                        chrs[index] = element[1];
                        break;
                    }
                }
            }
        }
        return new String(chrs);
    }
}
