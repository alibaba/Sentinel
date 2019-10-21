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

package org.apache.commons.codec.net;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringDecoder;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.binary.StringUtils;

/**
 * Codec for the Quoted-Printable section of <a href="http://www.ietf.org/rfc/rfc1521.txt">RFC 1521</a>.
 * <p>
 * The Quoted-Printable encoding is intended to represent data that largely consists of octets that correspond to
 * printable characters in the ASCII character set. It encodes the data in such a way that the resulting octets are
 * unlikely to be modified by mail transport. If the data being encoded are mostly ASCII text, the encoded form of the
 * data remains largely recognizable by humans. A body which is entirely ASCII may also be encoded in Quoted-Printable
 * to ensure the integrity of the data should the message pass through a character- translating, and/or line-wrapping
 * gateway.
 * <p>
 * Note:
 * <p>
 * Rules #3, #4, and #5 of the quoted-printable spec are not implemented yet because the complete quoted-printable spec
 * does not lend itself well into the byte[] oriented codec framework. Complete the codec once the streamable codec
 * framework is ready. The motivation behind providing the codec in a partial form is that it can already come in handy
 * for those applications that do not require quoted-printable line formatting (rules #3, #4, #5), for instance Q codec.
 * <p>
 * This class is immutable and thread-safe.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc1521.txt"> RFC 1521 MIME (Multipurpose Internet Mail Extensions) Part One:
 *          Mechanisms for Specifying and Describing the Format of Internet Message Bodies </a>
 *
 * @since 1.3
 * @version $Id: QuotedPrintableCodec.java 1429868 2013-01-07 16:08:05Z ggregory $
 */
public class QuotedPrintableCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    /**
     * The default charset used for string decoding and encoding.
     */
    private final Charset charset;

    /**
     * BitSet of printable characters as defined in RFC 1521.
     */
    private static final BitSet PRINTABLE_CHARS = new BitSet(256);

    private static final byte ESCAPE_CHAR = '=';

    private static final byte TAB = 9;

    private static final byte SPACE = 32;
    // Static initializer for printable chars collection
    static {
        // alpha characters
        for (int i = 33; i <= 60; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 62; i <= 126; i++) {
            PRINTABLE_CHARS.set(i);
        }
        PRINTABLE_CHARS.set(TAB);
        PRINTABLE_CHARS.set(SPACE);
    }

    /**
     * Default constructor, assumes default charset of {@link Charsets#UTF_8}
     */
    public QuotedPrintableCodec() {
        this(Charsets.UTF_8);
    }

    /**
     * Constructor which allows for the selection of a default charset.
     *
     * @param charset
     *            the default string charset to use.
     * @since 1.7
     */
    public QuotedPrintableCodec(final Charset charset) {
        this.charset = charset;
    }

    /**
     * Constructor which allows for the selection of a default charset.
     *
     * @param charsetName
     *            the default string charset to use.
     * @throws UnsupportedCharsetException
     *             If no support for the named charset is available
     *             in this instance of the Java virtual machine
     * @throws IllegalArgumentException
     *             If the given charsetName is null
     * @throws IllegalCharsetNameException
     *             If the given charset name is illegal
     *
     * @since 1.7 throws UnsupportedCharsetException if the named charset is unavailable
     */
    public QuotedPrintableCodec(final String charsetName)
            throws IllegalCharsetNameException, IllegalArgumentException, UnsupportedCharsetException {
        this(Charset.forName(charsetName));
    }

    /**
     * Encodes byte into its quoted-printable representation.
     *
     * @param b
     *            byte to encode
     * @param buffer
     *            the buffer to write to
     */
    private static final void encodeQuotedPrintable(final int b, final ByteArrayOutputStream buffer) {
        buffer.write(ESCAPE_CHAR);
        final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
        final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
        buffer.write(hex1);
        buffer.write(hex2);
    }

    /**
     * Encodes an array of bytes into an array of quoted-printable 7-bit characters. Unsafe characters are escaped.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data and unformatted text.
     *
     * @param printable
     *            bitset of characters deemed quoted-printable
     * @param bytes
     *            array of bytes to be encoded
     * @return array of bytes containing quoted-printable data
     */
    public static final byte[] encodeQuotedPrintable(BitSet printable, final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (printable == null) {
            printable = PRINTABLE_CHARS;
        }
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (final byte c : bytes) {
            int b = c;
            if (b < 0) {
                b = 256 + b;
            }
            if (printable.get(b)) {
                buffer.write(b);
            } else {
                encodeQuotedPrintable(b, buffer);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Decodes an array quoted-printable characters into an array of original bytes. Escaped characters are converted
     * back to their original representation.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521.
     *
     * @param bytes
     *            array of quoted-printable characters
     * @return array of original bytes
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful
     */
    public static final byte[] decodeQuotedPrintable(final byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    final int u = Utils.digit16(bytes[++i]);
                    final int l = Utils.digit16(bytes[++i]);
                    buffer.write((char) ((u << 4) + l));
                } catch (final ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid quoted-printable encoding", e);
                }
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Encodes an array of bytes into an array of quoted-printable 7-bit characters. Unsafe characters are escaped.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data and unformatted text.
     *
     * @param bytes
     *            array of bytes to be encoded
     * @return array of bytes containing quoted-printable data
     */
    @Override
    public byte[] encode(final byte[] bytes) {
        return encodeQuotedPrintable(PRINTABLE_CHARS, bytes);
    }

    /**
     * Decodes an array of quoted-printable characters into an array of original bytes. Escaped characters are converted
     * back to their original representation.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521.
     *
     * @param bytes
     *            array of quoted-printable characters
     * @return array of original bytes
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful
     */
    @Override
    public byte[] decode(final byte[] bytes) throws DecoderException {
        return decodeQuotedPrintable(bytes);
    }

    /**
     * Encodes a string into its quoted-printable form using the default string charset. Unsafe characters are escaped.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data.
     *
     * @param str
     *            string to convert to quoted-printable form
     * @return quoted-printable string
     * @throws EncoderException
     *             Thrown if quoted-printable encoding is unsuccessful
     *
     * @see #getCharset()
     */
    @Override
    public String encode(final String str) throws EncoderException {
        return this.encode(str, getCharset());
    }

    /**
     * Decodes a quoted-printable string into its original form using the specified string charset. Escaped characters
     * are converted back to their original representation.
     *
     * @param str
     *            quoted-printable string to convert into its original form
     * @param charset
     *            the original string charset
     * @return original string
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful
     * @since 1.7
     */
    public String decode(final String str, final Charset charset) throws DecoderException {
        if (str == null) {
            return null;
        }
        return new String(this.decode(StringUtils.getBytesUsAscii(str)), charset);
    }

    /**
     * Decodes a quoted-printable string into its original form using the specified string charset. Escaped characters
     * are converted back to their original representation.
     *
     * @param str
     *            quoted-printable string to convert into its original form
     * @param charset
     *            the original string charset
     * @return original string
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful
     * @throws UnsupportedEncodingException
     *             Thrown if charset is not supported
     */
    public String decode(final String str, final String charset) throws DecoderException, UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        return new String(decode(StringUtils.getBytesUsAscii(str)), charset);
    }

    /**
     * Decodes a quoted-printable string into its original form using the default string charset. Escaped characters are
     * converted back to their original representation.
     *
     * @param str
     *            quoted-printable string to convert into its original form
     * @return original string
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful. Thrown if charset is not supported.
     * @see #getCharset()
     */
    @Override
    public String decode(final String str) throws DecoderException {
        return this.decode(str, this.getCharset());
    }

    /**
     * Encodes an object into its quoted-printable safe form. Unsafe characters are escaped.
     *
     * @param obj
     *            string to convert to a quoted-printable form
     * @return quoted-printable object
     * @throws EncoderException
     *             Thrown if quoted-printable encoding is not applicable to objects of this type or if encoding is
     *             unsuccessful
     */
    @Override
    public Object encode(final Object obj) throws EncoderException {
        if (obj == null) {
            return null;
        } else if (obj instanceof byte[]) {
            return encode((byte[]) obj);
        } else if (obj instanceof String) {
            return encode((String) obj);
        } else {
            throw new EncoderException("Objects of type " +
                  obj.getClass().getName() +
                  " cannot be quoted-printable encoded");
        }
    }

    /**
     * Decodes a quoted-printable object into its original form. Escaped characters are converted back to their original
     * representation.
     *
     * @param obj
     *            quoted-printable object to convert into its original form
     * @return original object
     * @throws DecoderException
     *             Thrown if the argument is not a <code>String</code> or <code>byte[]</code>. Thrown if a failure
     *             condition is encountered during the decode process.
     */
    @Override
    public Object decode(final Object obj) throws DecoderException {
        if (obj == null) {
            return null;
        } else if (obj instanceof byte[]) {
            return decode((byte[]) obj);
        } else if (obj instanceof String) {
            return decode((String) obj);
        } else {
            throw new DecoderException("Objects of type " +
                  obj.getClass().getName() +
                  " cannot be quoted-printable decoded");
        }
    }

    /**
     * Gets the default charset name used for string decoding and encoding.
     *
     * @return the default charset name
     * @since 1.7
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Gets the default charset name used for string decoding and encoding.
     *
     * @return the default charset name
     */
    public String getDefaultCharset() {
        return this.charset.name();
    }

    /**
     * Encodes a string into its quoted-printable form using the specified charset. Unsafe characters are escaped.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data and unformatted text.
     *
     * @param str
     *            string to convert to quoted-printable form
     * @param charset
     *            the charset for str
     * @return quoted-printable string
     * @since 1.7
     */
    public String encode(final String str, final Charset charset) {
        if (str == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(this.encode(str.getBytes(charset)));
    }

    /**
     * Encodes a string into its quoted-printable form using the specified charset. Unsafe characters are escaped.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data and unformatted text.
     *
     * @param str
     *            string to convert to quoted-printable form
     * @param charset
     *            the charset for str
     * @return quoted-printable string
     * @throws UnsupportedEncodingException
     *             Thrown if the charset is not supported
     */
    public String encode(final String str, final String charset) throws UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(encode(str.getBytes(charset)));
    }
}
