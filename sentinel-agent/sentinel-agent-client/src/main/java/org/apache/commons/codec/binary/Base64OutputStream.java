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

package org.apache.commons.codec.binary;

import java.io.OutputStream;

/**
 * Provides Base64 encoding and decoding in a streaming fashion (unlimited size). When encoding the default lineLength
 * is 76 characters and the default lineEnding is CRLF, but these can be overridden by using the appropriate
 * constructor.
 * <p>
 * The default behaviour of the Base64OutputStream is to ENCODE, whereas the default behaviour of the Base64InputStream
 * is to DECODE. But this behaviour can be overridden by using a different constructor.
 * </p>
 * <p>
 * This class implements section <cite>6.8. Base64 Content-Transfer-Encoding</cite> from RFC 2045 <cite>Multipurpose
 * Internet Mail Extensions (MIME) Part One: Format of Internet Message Bodies</cite> by Freed and Borenstein.
 * </p>
 * <p>
 * Since this class operates directly on byte streams, and not character streams, it is hard-coded to only encode/decode
 * character encodings which are compatible with the lower 127 ASCII chart (ISO-8859-1, Windows-1252, UTF-8, etc).
 * </p>
 *
 * @version $Id: Base64OutputStream.java 1435550 2013-01-19 14:09:52Z tn $
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
 * @since 1.4
 */
public class Base64OutputStream extends BaseNCodecOutputStream {

    /**
     * Creates a Base64OutputStream such that all data written is Base64-encoded to the original provided OutputStream.
     *
     * @param out
     *            OutputStream to wrap.
     */
    public Base64OutputStream(final OutputStream out) {
        this(out, true);
    }

    /**
     * Creates a Base64OutputStream such that all data written is either Base64-encoded or Base64-decoded to the
     * original provided OutputStream.
     *
     * @param out
     *            OutputStream to wrap.
     * @param doEncode
     *            true if we should encode all data written to us, false if we should decode.
     */
    public Base64OutputStream(final OutputStream out, final boolean doEncode) {
        super(out,new Base64(false), doEncode);
    }

    /**
     * Creates a Base64OutputStream such that all data written is either Base64-encoded or Base64-decoded to the
     * original provided OutputStream.
     *
     * @param out
     *            OutputStream to wrap.
     * @param doEncode
     *            true if we should encode all data written to us, false if we should decode.
     * @param lineLength
     *            If doEncode is true, each line of encoded data will contain lineLength characters (rounded down to
     *            nearest multiple of 4). If lineLength <=0, the encoded data is not divided into lines. If doEncode is
     *            false, lineLength is ignored.
     * @param lineSeparator
     *            If doEncode is true, each line of encoded data will be terminated with this byte sequence (e.g. \r\n).
     *            If lineLength <= 0, the lineSeparator is not used. If doEncode is false lineSeparator is ignored.
     */
    public Base64OutputStream(final OutputStream out, final boolean doEncode,
                              final int lineLength, final byte[] lineSeparator) {
        super(out, new Base64(lineLength, lineSeparator), doEncode);
    }
}
