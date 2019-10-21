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

import java.util.NoSuchElementException;

import org.apache.http.HeaderIterator;
import org.apache.http.ParseException;
import org.apache.http.TokenIterator;
import org.apache.http.util.Args;

/**
 * Basic implementation of a {@link TokenIterator}.
 * This implementation parses {@code #token} sequences as
 * defined by RFC 2616, section 2.
 * It extends that definition somewhat beyond US-ASCII.
 *
 * @since 4.0
 */
public class BasicTokenIterator implements TokenIterator {

    /** The HTTP separator characters. Defined in RFC 2616, section 2.2. */
    // the order of the characters here is adjusted to put the
    // most likely candidates at the beginning of the collection
    public final static String HTTP_SEPARATORS = " ,;=()<>@:\\\"/[]?{}\t";


    /** The iterator from which to obtain the next header. */
    protected final HeaderIterator headerIt;

    /**
     * The value of the current header.
     * This is the header value that includes {@link #currentToken}.
     * Undefined if the iteration is over.
     */
    protected String currentHeader;

    /**
     * The token to be returned by the next call to {@link #nextToken()}.
     * {@code null} if the iteration is over.
     */
    protected String currentToken;

    /**
     * The position after {@link #currentToken} in {@link #currentHeader}.
     * Undefined if the iteration is over.
     */
    protected int searchPos;


    /**
     * Creates a new instance of {@link BasicTokenIterator}.
     *
     * @param headerIterator    the iterator for the headers to tokenize
     */
    public BasicTokenIterator(final HeaderIterator headerIterator) {
        super();
        this.headerIt = Args.notNull(headerIterator, "Header iterator");
        this.searchPos = findNext(-1);
    }


    // non-javadoc, see interface TokenIterator
    @Override
    public boolean hasNext() {
        return (this.currentToken != null);
    }


    /**
     * Obtains the next token from this iteration.
     *
     * @return  the next token in this iteration
     *
     * @throws NoSuchElementException   if the iteration is already over
     * @throws ParseException   if an invalid header value is encountered
     */
    @Override
    public String nextToken()
        throws NoSuchElementException, ParseException {

        if (this.currentToken == null) {
            throw new NoSuchElementException("Iteration already finished.");
        }

        final String result = this.currentToken;
        // updates currentToken, may trigger ParseException:
        this.searchPos = findNext(this.searchPos);

        return result;
    }


    /**
     * Returns the next token.
     * Same as {@link #nextToken}, but with generic return type.
     *
     * @return  the next token in this iteration
     *
     * @throws NoSuchElementException   if there are no more tokens
     * @throws ParseException   if an invalid header value is encountered
     */
    @Override
    public final Object next()
        throws NoSuchElementException, ParseException {
        return nextToken();
    }


    /**
     * Removing tokens is not supported.
     *
     * @throws UnsupportedOperationException    always
     */
    @Override
    public final void remove()
        throws UnsupportedOperationException {

        throw new UnsupportedOperationException
            ("Removing tokens is not supported.");
    }


    /**
     * Determines the next token.
     * If found, the token is stored in {@link #currentToken}.
     * The return value indicates the position after the token
     * in {@link #currentHeader}. If necessary, the next header
     * will be obtained from {@link #headerIt}.
     * If not found, {@link #currentToken} is set to {@code null}.
     *
     * @param pos       the position in the current header at which to
     *                  start the search, -1 to search in the first header
     *
     * @return  the position after the found token in the current header, or
     *          negative if there was no next token
     *
     * @throws ParseException   if an invalid header value is encountered
     */
    protected int findNext(final int pos) throws ParseException {
        int from = pos;
        if (from < 0) {
            // called from the constructor, initialize the first header
            if (!this.headerIt.hasNext()) {
                return -1;
            }
            this.currentHeader = this.headerIt.nextHeader().getValue();
            from = 0;
        } else {
            // called after a token, make sure there is a separator
            from = findTokenSeparator(from);
        }

        final int start = findTokenStart(from);
        if (start < 0) {
            this.currentToken = null;
            return -1; // nothing found
        }

        final int end = findTokenEnd(start);
        this.currentToken = createToken(this.currentHeader, start, end);
        return end;
    }


    /**
     * Creates a new token to be returned.
     * Called from {@link #findNext findNext} after the token is identified.
     * The default implementation simply calls
     * {@link java.lang.String#substring String.substring}.
     * <p>
     * If header values are significantly longer than tokens, and some
     * tokens are permanently referenced by the application, there can
     * be problems with garbage collection. A substring will hold a
     * reference to the full characters of the original string and
     * therefore occupies more memory than might be expected.
     * To avoid this, override this method and create a new string
     * instead of a substring.
     * </p>
     *
     * @param value     the full header value from which to create a token
     * @param start     the index of the first token character
     * @param end       the index after the last token character
     *
     * @return  a string representing the token identified by the arguments
     */
    protected String createToken(final String value, final int start, final int end) {
        return value.substring(start, end);
    }


    /**
     * Determines the starting position of the next token.
     * This method will iterate over headers if necessary.
     *
     * @param pos       the position in the current header at which to
     *                  start the search
     *
     * @return  the position of the token start in the current header,
     *          negative if no token start could be found
     */
    protected int findTokenStart(final int pos) {
        int from = Args.notNegative(pos, "Search position");
        boolean found = false;
        while (!found && (this.currentHeader != null)) {

            final int to = this.currentHeader.length();
            while (!found && (from < to)) {

                final char ch = this.currentHeader.charAt(from);
                if (isTokenSeparator(ch) || isWhitespace(ch)) {
                    // whitspace and token separators are skipped
                    from++;
                } else if (isTokenChar(this.currentHeader.charAt(from))) {
                    // found the start of a token
                    found = true;
                } else {
                    throw new ParseException
                        ("Invalid character before token (pos " + from +
                         "): " + this.currentHeader);
                }
            }
            if (!found) {
                if (this.headerIt.hasNext()) {
                    this.currentHeader = this.headerIt.nextHeader().getValue();
                    from = 0;
                } else {
                    this.currentHeader = null;
                }
            }
        } // while headers

        return found ? from : -1;
    }


    /**
     * Determines the position of the next token separator.
     * Because of multi-header joining rules, the end of a
     * header value is a token separator. This method does
     * therefore not need to iterate over headers.
     *
     * @param pos       the position in the current header at which to
     *                  start the search
     *
     * @return  the position of a token separator in the current header,
     *          or at the end
     *
     * @throws ParseException
     *         if a new token is found before a token separator.
     *         RFC 2616, section 2.1 explicitly requires a comma between
     *         tokens for {@code #}.
     */
    protected int findTokenSeparator(final int pos) {
        int from = Args.notNegative(pos, "Search position");
        boolean found = false;
        final int to = this.currentHeader.length();
        while (!found && (from < to)) {
            final char ch = this.currentHeader.charAt(from);
            if (isTokenSeparator(ch)) {
                found = true;
            } else if (isWhitespace(ch)) {
                from++;
            } else if (isTokenChar(ch)) {
                throw new ParseException
                    ("Tokens without separator (pos " + from +
                     "): " + this.currentHeader);
            } else {
                throw new ParseException
                    ("Invalid character after token (pos " + from +
                     "): " + this.currentHeader);
            }
        }

        return from;
    }


    /**
     * Determines the ending position of the current token.
     * This method will not leave the current header value,
     * since the end of the header value is a token boundary.
     *
     * @param from      the position of the first character of the token
     *
     * @return  the position after the last character of the token.
     *          The behavior is undefined if {@code from} does not
     *          point to a token character in the current header value.
     */
    protected int findTokenEnd(final int from) {
        Args.notNegative(from, "Search position");
        final int to = this.currentHeader.length();
        int end = from+1;
        while ((end < to) && isTokenChar(this.currentHeader.charAt(end))) {
            end++;
        }

        return end;
    }


    /**
     * Checks whether a character is a token separator.
     * RFC 2616, section 2.1 defines comma as the separator for
     * {@code #token} sequences. The end of a header value will
     * also separate tokens, but that is not a character check.
     *
     * @param ch        the character to check
     *
     * @return  {@code true} if the character is a token separator,
     *          {@code false} otherwise
     */
    protected boolean isTokenSeparator(final char ch) {
        return (ch == ',');
    }


    /**
     * Checks whether a character is a whitespace character.
     * RFC 2616, section 2.2 defines space and horizontal tab as whitespace.
     * The optional preceeding line break is irrelevant, since header
     * continuation is handled transparently when parsing messages.
     *
     * @param ch        the character to check
     *
     * @return  {@code true} if the character is whitespace,
     *          {@code false} otherwise
     */
    protected boolean isWhitespace(final char ch) {

        // we do not use Character.isWhitspace(ch) here, since that allows
        // many control characters which are not whitespace as per RFC 2616
        return ((ch == '\t') || Character.isSpaceChar(ch));
    }


    /**
     * Checks whether a character is a valid token character.
     * Whitespace, control characters, and HTTP separators are not
     * valid token characters. The HTTP specification (RFC 2616, section 2.2)
     * defines tokens only for the US-ASCII character set, this
     * method extends the definition to other character sets.
     *
     * @param ch        the character to check
     *
     * @return  {@code true} if the character is a valid token start,
     *          {@code false} otherwise
     */
    protected boolean isTokenChar(final char ch) {

        // common sense extension of ALPHA + DIGIT
        if (Character.isLetterOrDigit(ch)) {
            return true;
        }

        // common sense extension of CTL
        if (Character.isISOControl(ch)) {
            return false;
        }

        // no common sense extension for this
        if (isHttpSeparator(ch)) {
            return false;
        }

        // RFC 2616, section 2.2 defines a token character as
        // "any CHAR except CTLs or separators". The controls
        // and separators are included in the checks above.
        // This will yield unexpected results for Unicode format characters.
        // If that is a problem, overwrite isHttpSeparator(char) to filter
        // out the false positives.
        return true;
    }


    /**
     * Checks whether a character is an HTTP separator.
     * The implementation in this class checks only for the HTTP separators
     * defined in RFC 2616, section 2.2. If you need to detect other
     * separators beyond the US-ASCII character set, override this method.
     *
     * @param ch        the character to check
     *
     * @return  {@code true} if the character is an HTTP separator
     */
    protected boolean isHttpSeparator(final char ch) {
        return (HTTP_SEPARATORS.indexOf(ch) >= 0);
    }


} // class BasicTokenIterator

