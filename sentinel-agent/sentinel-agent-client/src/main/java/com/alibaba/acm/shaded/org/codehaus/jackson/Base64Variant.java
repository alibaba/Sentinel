/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code and binary code bundles.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.acm.shaded.org.codehaus.jackson;

import java.util.Arrays;

/**
 * Abstract base class used to define specific details of which
 * variant of Base64 encoding/decoding is to be used. Although there is
 * somewhat standard basic version (so-called "MIME Base64"), other variants
 * exists, see <a href="http://en.wikipedia.org/wiki/Base64">Base64 Wikipedia entry</a> for details.
 * 
 * @author Tatu Saloranta
 *
 * @since 0.9.3
 */
public final class Base64Variant
{
    /**
     * Placeholder used by "no padding" variant, to be used when a character
     * value is needed.
     */
    final static char PADDING_CHAR_NONE = '\0';

    /**
     * Marker used to denote ascii characters that do not correspond
     * to a 6-bit value (in this variant), and is not used as a padding
     * character.
     */
    public final static int BASE64_VALUE_INVALID = -1;

    /**
     * Marker used to denote ascii character (in decoding table) that
     * is the padding character using this variant (if any).
     */
    public final static int BASE64_VALUE_PADDING = -2;

    /*
    /**********************************************************
    /* Encoding/decoding tables
    /**********************************************************
     */

    /**
     * Decoding table used for base 64 decoding.
     */
    private final int[] _asciiToBase64 = new int[128];

    /**
     * Encoding table used for base 64 decoding when output is done
     * as characters.
     */
    private final char[] _base64ToAsciiC = new char[64];

    /**
     * Alternative encoding table used for base 64 decoding when output is done
     * as ascii bytes.
     */
    private final byte[] _base64ToAsciiB = new byte[64];

    /*
    /**********************************************************
    /* Other configuration
    /**********************************************************
     */

    /**
     * Symbolic name of variant; used for diagnostics/debugging.
     */
    final String _name;

    /**
     * Whether this variant uses padding or not.
     */
    final boolean _usesPadding;

    /**
     * Characted used for padding, if any ({@link #PADDING_CHAR_NONE} if not).
     */
    final char _paddingChar;
    
    /**
     * Maximum number of encoded base64 characters to output during encoding
     * before adding a linefeed, if line length is to be limited
     * ({@link java.lang.Integer#MAX_VALUE} if not limited).
     *<p>
     * Note: for some output modes (when writing attributes) linefeeds may
     * need to be avoided, and this value ignored.
     */
    final int _maxLineLength;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public Base64Variant(String name, String base64Alphabet, boolean usesPadding, char paddingChar, int maxLineLength)
    {
        _name = name;
        _usesPadding = usesPadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;

        // Ok and then we need to create codec tables.

        // First the main encoding table:
        int alphaLen = base64Alphabet.length();
        if (alphaLen != 64) {
            throw new IllegalArgumentException("Base64Alphabet length must be exactly 64 (was "+alphaLen+")");
        }

        // And then secondary encoding table and decoding table:
        base64Alphabet.getChars(0, alphaLen, _base64ToAsciiC, 0);
        Arrays.fill(_asciiToBase64, BASE64_VALUE_INVALID);
        for (int i = 0; i < alphaLen; ++i) {
            char alpha = _base64ToAsciiC[i];
            _base64ToAsciiB[i] = (byte) alpha;
            _asciiToBase64[alpha] = i;
        }

        // Plus if we use padding, add that in too
        if (usesPadding) {
            _asciiToBase64[(int) paddingChar] = BASE64_VALUE_PADDING;
        }
    }

    /**
     * "Copy constructor" that can be used when the base alphabet is identical
     * to one used by another variant except for the maximum line length
     * (and obviously, name).
     */
    public Base64Variant(Base64Variant base, String name, int maxLineLength)
    {
        this(base, name, base._usesPadding, base._paddingChar, maxLineLength);
    }

    /**
     * "Copy constructor" that can be used when the base alphabet is identical
     * to one used by another variant, but other details (padding, maximum
     * line length) differ
     */
    public Base64Variant(Base64Variant base, String name, boolean usesPadding, char paddingChar, int maxLineLength)
    {
        _name = name;
        byte[] srcB = base._base64ToAsciiB;
        System.arraycopy(srcB, 0, this._base64ToAsciiB, 0, srcB.length);
        char[] srcC = base._base64ToAsciiC;
        System.arraycopy(srcC, 0, this._base64ToAsciiC, 0, srcC.length);
        int[] srcV = base._asciiToBase64;
        System.arraycopy(srcV, 0, this._asciiToBase64, 0, srcV.length);

        _usesPadding = usesPadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;
    }

    /*
    /**********************************************************
    /* Public accessors
    /**********************************************************
     */

    public String getName() { return _name; }

    public boolean usesPadding() { return _usesPadding; }
    public boolean usesPaddingChar(char c) { return c == _paddingChar; }
    public boolean usesPaddingChar(int ch) { return ch == (int) _paddingChar; }
    public char getPaddingChar() { return _paddingChar; }
    public byte getPaddingByte() { return (byte)_paddingChar; }

    public int getMaxLineLength() { return _maxLineLength; }

    /*
    /**********************************************************
    /* Decoding support
    /**********************************************************
     */

    /**
     * @return 6-bit decoded value, if valid character; 
     */
    public int decodeBase64Char(char c)
    {
        int ch = (int) c;
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }

    public int decodeBase64Char(int ch)
    {
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }

    public int decodeBase64Byte(byte b)
    {
        int ch = (int) b;
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }

    /*
    /**********************************************************
    /* Encoding support
    /**********************************************************
     */

    public char encodeBase64BitsAsChar(int value)
    {
        /* Let's assume caller has done necessary checks; this
         * method must be fast and inlinable
         */
        return _base64ToAsciiC[value];
    }

    /**
     * Method that encodes given right-aligned (LSB) 24-bit value
     * into 4 base64 characters, stored in given result buffer.
     */
    public int encodeBase64Chunk(int b24, char[] buffer, int ptr)
    {
        buffer[ptr++] = _base64ToAsciiC[(b24 >> 18) & 0x3F];
        buffer[ptr++] = _base64ToAsciiC[(b24 >> 12) & 0x3F];
        buffer[ptr++] = _base64ToAsciiC[(b24 >> 6) & 0x3F];
        buffer[ptr++] = _base64ToAsciiC[b24 & 0x3F];
        return ptr;
    }

    public void encodeBase64Chunk(StringBuilder sb, int b24)
    {
        sb.append(_base64ToAsciiC[(b24 >> 18) & 0x3F]);
        sb.append(_base64ToAsciiC[(b24 >> 12) & 0x3F]);
        sb.append(_base64ToAsciiC[(b24 >> 6) & 0x3F]);
        sb.append(_base64ToAsciiC[b24 & 0x3F]);
    }

    /**
     * Method that outputs partial chunk (which only encodes one
     * or two bytes of data). Data given is still aligned same as if
     * it as full data; that is, missing data is at the "right end"
     * (LSB) of int.
     *
     * @param outputBytes Number of encoded bytes included (either 1 or 2)
     */
    public int encodeBase64Partial(int bits, int outputBytes, char[] buffer, int outPtr)
    {
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 12) & 0x3F];
        if (_usesPadding) {
            buffer[outPtr++] = (outputBytes == 2) ?
                _base64ToAsciiC[(bits >> 6) & 0x3F] : _paddingChar;
            buffer[outPtr++] = _paddingChar;
        } else {
            if (outputBytes == 2) {
                buffer[outPtr++] = _base64ToAsciiC[(bits >> 6) & 0x3F];
            }
        }
        return outPtr;
    }

    public void encodeBase64Partial(StringBuilder sb, int bits, int outputBytes)
    {
        sb.append(_base64ToAsciiC[(bits >> 18) & 0x3F]);
        sb.append(_base64ToAsciiC[(bits >> 12) & 0x3F]);
        if (_usesPadding) {
            sb.append((outputBytes == 2) ?
                      _base64ToAsciiC[(bits >> 6) & 0x3F] : _paddingChar);
            sb.append(_paddingChar);
        } else {
            if (outputBytes == 2) {
                sb.append(_base64ToAsciiC[(bits >> 6) & 0x3F]);
            }
        }
    }

    public byte encodeBase64BitsAsByte(int value)
    {
        // As with above, assuming it is 6-bit value
        return _base64ToAsciiB[value];
    }

    /**
     * Method that encodes given right-aligned (LSB) 24-bit value
     * into 4 base64 bytes (ascii), stored in given result buffer.
     */
    public int encodeBase64Chunk(int b24, byte[] buffer, int ptr)
    {
        buffer[ptr++] = _base64ToAsciiB[(b24 >> 18) & 0x3F];
        buffer[ptr++] = _base64ToAsciiB[(b24 >> 12) & 0x3F];
        buffer[ptr++] = _base64ToAsciiB[(b24 >> 6) & 0x3F];
        buffer[ptr++] = _base64ToAsciiB[b24 & 0x3F];
        return ptr;
    }

    /**
     * Method that outputs partial chunk (which only encodes one
     * or two bytes of data). Data given is still aligned same as if
     * it as full data; that is, missing data is at the "right end"
     * (LSB) of int.
     *
     * @param outputBytes Number of encoded bytes included (either 1 or 2)
     */
    public int encodeBase64Partial(int bits, int outputBytes, byte[] buffer, int outPtr)
    {
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 12) & 0x3F];
        if (_usesPadding) {
            byte pb = (byte) _paddingChar;
            buffer[outPtr++] = (outputBytes == 2) ?
                _base64ToAsciiB[(bits >> 6) & 0x3F] : pb;
            buffer[outPtr++] = pb;
        } else {
            if (outputBytes == 2) {
                buffer[outPtr++] = _base64ToAsciiB[(bits >> 6) & 0x3F];
            }
        }
        return outPtr;
    }

    /**
     * Convenience method for converting given byte array as base64 encoded
     * String using this variant's settings.
     * Resulting value is "raw", that is, not enclosed in double-quotes.
     * 
     * @param input Byte array to encode
     * 
     * @since 1.6
     */
    public String encode(byte[] input)
    {
        return encode(input, false);
    }

    /**
     * Convenience method for converting given byte array as base64 encoded
     * String using this variant's settings, optionally enclosed in
     * double-quotes.
     * 
     * @param input Byte array to encode
     * @param addQuotes Whether to surround resulting value in double quotes
     *   or not
     * 
     * @since 1.6
     */
    public String encode(byte[] input, boolean addQuotes)
    {
        int inputEnd = input.length;
        StringBuilder sb;
        {
            // let's approximate... 33% overhead, ~= 3/8 (0.375)
            int outputLen = inputEnd + (inputEnd >> 2) + (inputEnd >> 3);
            sb = new StringBuilder(outputLen);
        }
        if (addQuotes) {
            sb.append('"');
        }

        int chunksBeforeLF = getMaxLineLength() >> 2;

        // Ok, first we loop through all full triplets of data:
        int inputPtr = 0;
        int safeInputEnd = inputEnd-3; // to get only full triplets

        while (inputPtr <= safeInputEnd) {
            // First, mash 3 bytes into lsb of 32-bit int
            int b24 = ((int) input[inputPtr++]) << 8;
            b24 |= ((int) input[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) input[inputPtr++]) & 0xFF);
            encodeBase64Chunk(sb, b24);
            if (--chunksBeforeLF <= 0) {
                // note: must quote in JSON value, so not really useful...
                sb.append('\\');
                sb.append('n');
                chunksBeforeLF = getMaxLineLength() >> 2;
            }
        }

        // And then we may have 1 or 2 leftover bytes to encode
        int inputLeft = inputEnd - inputPtr; // 0, 1 or 2
        if (inputLeft > 0) { // yes, but do we have room for output?
            int b24 = ((int) input[inputPtr++]) << 16;
            if (inputLeft == 2) {
                b24 |= (((int) input[inputPtr++]) & 0xFF) << 8;
            }
            encodeBase64Partial(sb, b24, inputLeft);
        }

        if (addQuotes) {
            sb.append('"');
        }
        return sb.toString();
    }
    
    /*
    /**********************************************************
    /* other methods
    /**********************************************************
     */

    @Override
    public String toString() { return _name; }
}

