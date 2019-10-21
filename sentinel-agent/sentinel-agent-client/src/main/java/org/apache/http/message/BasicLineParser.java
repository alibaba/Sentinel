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

package org.apache.http.message;

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/**
 * Basic parser for lines in the head section of an HTTP message.
 * There are individual methods for parsing a request line, a
 * status line, or a header line.
 * The lines to parse are passed in memory, the parser does not depend
 * on any specific IO mechanism.
 * Instances of this class are stateless and thread-safe.
 * Derived classes MUST maintain these properties.
 *
 * <p>
 * Note: This class was created by refactoring parsing code located in
 * various other classes. The author tags from those other classes have
 * been replicated here, although the association with the parsing code
 * taken from there has not been traced.
 * </p>
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicLineParser implements LineParser {

    /**
     * A default instance of this class, for use as default or fallback.
     * Note that {@link BasicLineParser} is not a singleton, there can
     * be many instances of the class itself and of derived classes.
     * The instance here provides non-customized, default behavior.
     *
     * @deprecated (4.3) use {@link #INSTANCE}
     */
    @Deprecated
    public final static BasicLineParser DEFAULT = new BasicLineParser();

    public final static BasicLineParser INSTANCE = new BasicLineParser();

    /**
     * A version of the protocol to parse.
     * The version is typically not relevant, but the protocol name.
     */
    protected final ProtocolVersion protocol;


    /**
     * Creates a new line parser for the given HTTP-like protocol.
     *
     * @param proto     a version of the protocol to parse, or
     *                  {@code null} for HTTP. The actual version
     *                  is not relevant, only the protocol name.
     */
    public BasicLineParser(final ProtocolVersion proto) {
        this.protocol = proto != null? proto : HttpVersion.HTTP_1_1;
    }


    /**
     * Creates a new line parser for HTTP.
     */
    public BasicLineParser() {
        this(null);
    }


    public static
        ProtocolVersion parseProtocolVersion(final String value,
                                             final LineParser parser) throws ParseException {
        Args.notNull(value, "Value");

        final CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        final ParserCursor cursor = new ParserCursor(0, value.length());
        return (parser != null ? parser : BasicLineParser.INSTANCE)
                .parseProtocolVersion(buffer, cursor);
    }


    // non-javadoc, see interface LineParser
    @Override
    public ProtocolVersion parseProtocolVersion(final CharArrayBuffer buffer,
                                                final ParserCursor cursor) throws ParseException {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        final String protoname = this.protocol.getProtocol();
        final int protolength  = protoname.length();

        final int indexFrom = cursor.getPos();
        final int indexTo = cursor.getUpperBound();

        skipWhitespace(buffer, cursor);

        int i = cursor.getPos();

        // long enough for "HTTP/1.1"?
        if (i + protolength + 4 > indexTo) {
            throw new ParseException
                ("Not a valid protocol version: " +
                 buffer.substring(indexFrom, indexTo));
        }

        // check the protocol name and slash
        boolean ok = true;
        for (int j=0; ok && (j<protolength); j++) {
            ok = (buffer.charAt(i+j) == protoname.charAt(j));
        }
        if (ok) {
            ok = (buffer.charAt(i+protolength) == '/');
        }
        if (!ok) {
            throw new ParseException
                ("Not a valid protocol version: " +
                 buffer.substring(indexFrom, indexTo));
        }

        i += protolength+1;

        final int period = buffer.indexOf('.', i, indexTo);
        if (period == -1) {
            throw new ParseException
                ("Invalid protocol version number: " +
                 buffer.substring(indexFrom, indexTo));
        }
        final int major;
        try {
            major = Integer.parseInt(buffer.substringTrimmed(i, period));
        } catch (final NumberFormatException e) {
            throw new ParseException
                ("Invalid protocol major version number: " +
                 buffer.substring(indexFrom, indexTo));
        }
        i = period + 1;

        int blank = buffer.indexOf(' ', i, indexTo);
        if (blank == -1) {
            blank = indexTo;
        }
        final int minor;
        try {
            minor = Integer.parseInt(buffer.substringTrimmed(i, blank));
        } catch (final NumberFormatException e) {
            throw new ParseException(
                "Invalid protocol minor version number: " +
                buffer.substring(indexFrom, indexTo));
        }

        cursor.updatePos(blank);

        return createProtocolVersion(major, minor);

    } // parseProtocolVersion


    /**
     * Creates a protocol version.
     * Called from {@link #parseProtocolVersion}.
     *
     * @param major     the major version number, for example 1 in HTTP/1.0
     * @param minor     the minor version number, for example 0 in HTTP/1.0
     *
     * @return  the protocol version
     */
    protected ProtocolVersion createProtocolVersion(final int major, final int minor) {
        return protocol.forVersion(major, minor);
    }



    // non-javadoc, see interface LineParser
    @Override
    public boolean hasProtocolVersion(final CharArrayBuffer buffer,
                                      final ParserCursor cursor) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        int index = cursor.getPos();

        final String protoname = this.protocol.getProtocol();
        final int  protolength = protoname.length();

        if (buffer.length() < protolength+4)
         {
            return false; // not long enough for "HTTP/1.1"
        }

        if (index < 0) {
            // end of line, no tolerance for trailing whitespace
            // this works only for single-digit major and minor version
            index = buffer.length() -4 -protolength;
        } else if (index == 0) {
            // beginning of line, tolerate leading whitespace
            while ((index < buffer.length()) &&
                    HTTP.isWhitespace(buffer.charAt(index))) {
                 index++;
             }
        } // else within line, don't tolerate whitespace


        if (index + protolength + 4 > buffer.length()) {
            return false;
        }


        // just check protocol name and slash, no need to analyse the version
        boolean ok = true;
        for (int j=0; ok && (j<protolength); j++) {
            ok = (buffer.charAt(index+j) == protoname.charAt(j));
        }
        if (ok) {
            ok = (buffer.charAt(index+protolength) == '/');
        }

        return ok;
    }



    public static
        RequestLine parseRequestLine(final String value,
                                     final LineParser parser) throws ParseException {
        Args.notNull(value, "Value");

        final CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        final ParserCursor cursor = new ParserCursor(0, value.length());
        return (parser != null ? parser : BasicLineParser.INSTANCE)
            .parseRequestLine(buffer, cursor);
    }


    /**
     * Parses a request line.
     *
     * @param buffer    a buffer holding the line to parse
     *
     * @return  the parsed request line
     *
     * @throws ParseException        in case of a parse error
     */
    @Override
    public RequestLine parseRequestLine(final CharArrayBuffer buffer,
                                        final ParserCursor cursor) throws ParseException {

        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        final int indexFrom = cursor.getPos();
        final int indexTo = cursor.getUpperBound();

        try {
            skipWhitespace(buffer, cursor);
            int i = cursor.getPos();

            int blank = buffer.indexOf(' ', i, indexTo);
            if (blank < 0) {
                throw new ParseException("Invalid request line: " +
                        buffer.substring(indexFrom, indexTo));
            }
            final String method = buffer.substringTrimmed(i, blank);
            cursor.updatePos(blank);

            skipWhitespace(buffer, cursor);
            i = cursor.getPos();

            blank = buffer.indexOf(' ', i, indexTo);
            if (blank < 0) {
                throw new ParseException("Invalid request line: " +
                        buffer.substring(indexFrom, indexTo));
            }
            final String uri = buffer.substringTrimmed(i, blank);
            cursor.updatePos(blank);

            final ProtocolVersion ver = parseProtocolVersion(buffer, cursor);

            skipWhitespace(buffer, cursor);
            if (!cursor.atEnd()) {
                throw new ParseException("Invalid request line: " +
                        buffer.substring(indexFrom, indexTo));
            }

            return createRequestLine(method, uri, ver);
        } catch (final IndexOutOfBoundsException e) {
            throw new ParseException("Invalid request line: " +
                                     buffer.substring(indexFrom, indexTo));
        }
    } // parseRequestLine


    /**
     * Instantiates a new request line.
     * Called from {@link #parseRequestLine}.
     *
     * @param method    the request method
     * @param uri       the requested URI
     * @param ver       the protocol version
     *
     * @return  a new status line with the given data
     */
    protected RequestLine createRequestLine(final String method,
                                            final String uri,
                                            final ProtocolVersion ver) {
        return new BasicRequestLine(method, uri, ver);
    }



    public static
        StatusLine parseStatusLine(final String value,
                                   final LineParser parser) throws ParseException {
        Args.notNull(value, "Value");

        final CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        final ParserCursor cursor = new ParserCursor(0, value.length());
        return (parser != null ? parser : BasicLineParser.INSTANCE)
                .parseStatusLine(buffer, cursor);
    }


    // non-javadoc, see interface LineParser
    @Override
    public StatusLine parseStatusLine(final CharArrayBuffer buffer,
                                      final ParserCursor cursor) throws ParseException {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        final int indexFrom = cursor.getPos();
        final int indexTo = cursor.getUpperBound();

        try {
            // handle the HTTP-Version
            final ProtocolVersion ver = parseProtocolVersion(buffer, cursor);

            // handle the Status-Code
            skipWhitespace(buffer, cursor);
            int i = cursor.getPos();

            int blank = buffer.indexOf(' ', i, indexTo);
            if (blank < 0) {
                blank = indexTo;
            }
            final int statusCode;
            final String s = buffer.substringTrimmed(i, blank);
            for (int j = 0; j < s.length(); j++) {
                if (!Character.isDigit(s.charAt(j))) {
                    throw new ParseException(
                            "Status line contains invalid status code: "
                            + buffer.substring(indexFrom, indexTo));
                }
            }
            try {
                statusCode = Integer.parseInt(s);
            } catch (final NumberFormatException e) {
                throw new ParseException(
                        "Status line contains invalid status code: "
                        + buffer.substring(indexFrom, indexTo));
            }
            //handle the Reason-Phrase
            i = blank;
            final String reasonPhrase;
            if (i < indexTo) {
                reasonPhrase = buffer.substringTrimmed(i, indexTo);
            } else {
                reasonPhrase = "";
            }
            return createStatusLine(ver, statusCode, reasonPhrase);

        } catch (final IndexOutOfBoundsException e) {
            throw new ParseException("Invalid status line: " +
                                     buffer.substring(indexFrom, indexTo));
        }
    } // parseStatusLine


    /**
     * Instantiates a new status line.
     * Called from {@link #parseStatusLine}.
     *
     * @param ver       the protocol version
     * @param status    the status code
     * @param reason    the reason phrase
     *
     * @return  a new status line with the given data
     */
    protected StatusLine createStatusLine(final ProtocolVersion ver,
                                          final int status,
                                          final String reason) {
        return new BasicStatusLine(ver, status, reason);
    }



    public static
        Header parseHeader(final String value,
                           final LineParser parser) throws ParseException {
        Args.notNull(value, "Value");

        final CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        return (parser != null ? parser : BasicLineParser.INSTANCE)
                .parseHeader(buffer);
    }


    // non-javadoc, see interface LineParser
    @Override
    public Header parseHeader(final CharArrayBuffer buffer)
        throws ParseException {

        // the actual parser code is in the constructor of BufferedHeader
        return new BufferedHeader(buffer);
    }


    /**
     * Helper to skip whitespace.
     */
    protected void skipWhitespace(final CharArrayBuffer buffer, final ParserCursor cursor) {
        int pos = cursor.getPos();
        final int indexTo = cursor.getUpperBound();
        while ((pos < indexTo) &&
               HTTP.isWhitespace(buffer.charAt(pos))) {
            pos++;
        }
        cursor.updatePos(pos);
    }

} // class BasicLineParser
