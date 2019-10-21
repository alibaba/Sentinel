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

package org.apache.http.util;

import java.io.Serializable;
import java.nio.CharBuffer;

import org.apache.http.protocol.HTTP;

/**
 * A resizable char array.
 *
 * @since 4.0
 */
public final class CharArrayBuffer implements CharSequence, Serializable {

    private static final long serialVersionUID = -6208952725094867135L;

    private char[] buffer;
    private int len;

    /**
     * Creates an instance of {@link CharArrayBuffer} with the given initial
     * capacity.
     *
     * @param capacity the capacity
     */
    public CharArrayBuffer(final int capacity) {
        super();
        Args.notNegative(capacity, "Buffer capacity");
        this.buffer = new char[capacity];
    }

    private void expand(final int newlen) {
        final char newbuffer[] = new char[Math.max(this.buffer.length << 1, newlen)];
        System.arraycopy(this.buffer, 0, newbuffer, 0, this.len);
        this.buffer = newbuffer;
    }

    /**
     * Appends {@code len} chars to this buffer from the given source
     * array starting at index {@code off}. The capacity of the buffer
     * is increased, if necessary, to accommodate all {@code len} chars.
     *
     * @param   b        the chars to be appended.
     * @param   off      the index of the first char to append.
     * @param   len      the number of chars to append.
     * @throws IndexOutOfBoundsException if {@code off} is out of
     * range, {@code len} is negative, or
     * {@code off} + {@code len} is out of range.
     */
    public void append(final char[] b, final int off, final int len) {
        if (b == null) {
            return;
        }
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) < 0) || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException("off: "+off+" len: "+len+" b.length: "+b.length);
        }
        if (len == 0) {
            return;
        }
        final int newlen = this.len + len;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        System.arraycopy(b, off, this.buffer, this.len, len);
        this.len = newlen;
    }

    /**
     * Appends chars of the given string to this buffer. The capacity of the
     * buffer is increased, if necessary, to accommodate all chars.
     *
     * @param str    the string.
     */
    public void append(final String str) {
        final String s = str != null ? str : "null";
        final int strlen = s.length();
        final int newlen = this.len + strlen;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        s.getChars(0, strlen, this.buffer, this.len);
        this.len = newlen;
    }

    /**
     * Appends {@code len} chars to this buffer from the given source
     * buffer starting at index {@code off}. The capacity of the
     * destination buffer is increased, if necessary, to accommodate all
     * {@code len} chars.
     *
     * @param   b        the source buffer to be appended.
     * @param   off      the index of the first char to append.
     * @param   len      the number of chars to append.
     * @throws IndexOutOfBoundsException if {@code off} is out of
     * range, {@code len} is negative, or
     * {@code off} + {@code len} is out of range.
     */
    public void append(final CharArrayBuffer b, final int off, final int len) {
        if (b == null) {
            return;
        }
        append(b.buffer, off, len);
    }

    /**
     * Appends all chars to this buffer from the given source buffer starting
     * at index {@code 0}. The capacity of the destination buffer is
     * increased, if necessary, to accommodate all {@link #length()} chars.
     *
     * @param   b        the source buffer to be appended.
     */
    public void append(final CharArrayBuffer b) {
        if (b == null) {
            return;
        }
        append(b.buffer,0, b.len);
    }

    /**
     * Appends {@code ch} char to this buffer. The capacity of the buffer
     * is increased, if necessary, to accommodate the additional char.
     *
     * @param   ch        the char to be appended.
     */
    public void append(final char ch) {
        final int newlen = this.len + 1;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        this.buffer[this.len] = ch;
        this.len = newlen;
    }

    /**
     * Appends {@code len} bytes to this buffer from the given source
     * array starting at index {@code off}. The capacity of the buffer
     * is increased, if necessary, to accommodate all {@code len} bytes.
     * <p>
     * The bytes are converted to chars using simple cast.
     *
     * @param   b        the bytes to be appended.
     * @param   off      the index of the first byte to append.
     * @param   len      the number of bytes to append.
     * @throws IndexOutOfBoundsException if {@code off} is out of
     * range, {@code len} is negative, or
     * {@code off} + {@code len} is out of range.
     */
    public void append(final byte[] b, final int off, final int len) {
        if (b == null) {
            return;
        }
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) < 0) || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException("off: "+off+" len: "+len+" b.length: "+b.length);
        }
        if (len == 0) {
            return;
        }
        final int oldlen = this.len;
        final int newlen = oldlen + len;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        for (int i1 = off, i2 = oldlen; i2 < newlen; i1++, i2++) {
            this.buffer[i2] = (char) (b[i1] & 0xff);
        }
        this.len = newlen;
    }

    /**
     * Appends {@code len} bytes to this buffer from the given source
     * array starting at index {@code off}. The capacity of the buffer
     * is increased, if necessary, to accommodate all {@code len} bytes.
     * <p>
     * The bytes are converted to chars using simple cast.
     *
     * @param   b        the bytes to be appended.
     * @param   off      the index of the first byte to append.
     * @param   len      the number of bytes to append.
     * @throws IndexOutOfBoundsException if {@code off} is out of
     * range, {@code len} is negative, or
     * {@code off} + {@code len} is out of range.
     */
    public void append(final ByteArrayBuffer b, final int off, final int len) {
        if (b == null) {
            return;
        }
        append(b.buffer(), off, len);
    }

    /**
     * Appends chars of the textual representation of the given object to this
     * buffer. The capacity of the buffer is increased, if necessary, to
     * accommodate all chars.
     *
     * @param obj    the object.
     */
    public void append(final Object obj) {
        append(String.valueOf(obj));
    }

    /**
     * Clears content of the buffer. The underlying char array is not resized.
     */
    public void clear() {
        this.len = 0;
    }

    /**
     * Converts the content of this buffer to an array of chars.
     *
     * @return char array
     */
    public char[] toCharArray() {
        final char[] b = new char[this.len];
        if (this.len > 0) {
            System.arraycopy(this.buffer, 0, b, 0, this.len);
        }
        return b;
    }

    /**
     * Returns the {@code char} value in this buffer at the specified
     * index. The index argument must be greater than or equal to
     * {@code 0}, and less than the length of this buffer.
     *
     * @param      i   the index of the desired char value.
     * @return     the char value at the specified index.
     * @throws     IndexOutOfBoundsException  if {@code index} is
     *             negative or greater than or equal to {@link #length()}.
     */
    public char charAt(final int i) {
        return this.buffer[i];
    }

    /**
     * Returns reference to the underlying char array.
     *
     * @return the char array.
     */
    public char[] buffer() {
        return this.buffer;
    }

    /**
     * Returns the current capacity. The capacity is the amount of storage
     * available for newly appended chars, beyond which an allocation will
     * occur.
     *
     * @return  the current capacity
     */
    public int capacity() {
        return this.buffer.length;
    }

    /**
     * Returns the length of the buffer (char count).
     *
     * @return  the length of the buffer
     */
    public int length() {
        return this.len;
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum.
     * If the current capacity is less than the argument, then a new internal
     * array is allocated with greater capacity. If the {@code required}
     * argument is non-positive, this method takes no action.
     *
     * @param   required   the minimum required capacity.
     */
    public void ensureCapacity(final int required) {
        if (required <= 0) {
            return;
        }
        final int available = this.buffer.length - this.len;
        if (required > available) {
            expand(this.len + required);
        }
    }

    /**
     * Sets the length of the buffer. The new length value is expected to be
     * less than the current capacity and greater than or equal to
     * {@code 0}.
     *
     * @param      len   the new length
     * @throws     IndexOutOfBoundsException  if the
     *               {@code len} argument is greater than the current
     *               capacity of the buffer or less than {@code 0}.
     */
    public void setLength(final int len) {
        if (len < 0 || len > this.buffer.length) {
            throw new IndexOutOfBoundsException("len: "+len+" < 0 or > buffer len: "+this.buffer.length);
        }
        this.len = len;
    }

    /**
     * Returns {@code true} if this buffer is empty, that is, its
     * {@link #length()} is equal to {@code 0}.
     * @return {@code true} if this buffer is empty, {@code false}
     *   otherwise.
     */
    public boolean isEmpty() {
        return this.len == 0;
    }

    /**
     * Returns {@code true} if this buffer is full, that is, its
     * {@link #length()} is equal to its {@link #capacity()}.
     * @return {@code true} if this buffer is full, {@code false}
     *   otherwise.
     */
    public boolean isFull() {
        return this.len == this.buffer.length;
    }

    /**
     * Returns the index within this buffer of the first occurrence of the
     * specified character, starting the search at the specified
     * {@code beginIndex} and finishing at {@code endIndex}.
     * If no such character occurs in this buffer within the specified bounds,
     * {@code -1} is returned.
     * <p>
     * There is no restriction on the value of {@code beginIndex} and
     * {@code endIndex}. If {@code beginIndex} is negative,
     * it has the same effect as if it were zero. If {@code endIndex} is
     * greater than {@link #length()}, it has the same effect as if it were
     * {@link #length()}. If the {@code beginIndex} is greater than
     * the {@code endIndex}, {@code -1} is returned.
     *
     * @param   ch     the char to search for.
     * @param   from   the index to start the search from.
     * @param   to     the index to finish the search at.
     * @return  the index of the first occurrence of the character in the buffer
     *   within the given bounds, or {@code -1} if the character does
     *   not occur.
     */
    public int indexOf(final int ch, final int from, final int to) {
        int beginIndex = from;
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        int endIndex = to;
        if (endIndex > this.len) {
            endIndex = this.len;
        }
        if (beginIndex > endIndex) {
            return -1;
        }
        for (int i = beginIndex; i < endIndex; i++) {
            if (this.buffer[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index within this buffer of the first occurrence of the
     * specified character, starting the search at {@code 0} and finishing
     * at {@link #length()}. If no such character occurs in this buffer within
     * those bounds, {@code -1} is returned.
     *
     * @param   ch          the char to search for.
     * @return  the index of the first occurrence of the character in the
     *   buffer, or {@code -1} if the character does not occur.
     */
    public int indexOf(final int ch) {
        return indexOf(ch, 0, this.len);
    }

    /**
     * Returns a substring of this buffer. The substring begins at the specified
     * {@code beginIndex} and extends to the character at index
     * {@code endIndex - 1}.
     *
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @return     the specified substring.
     * @exception  StringIndexOutOfBoundsException  if the
     *             {@code beginIndex} is negative, or
     *             {@code endIndex} is larger than the length of this
     *             buffer, or {@code beginIndex} is larger than
     *             {@code endIndex}.
     */
    public String substring(final int beginIndex, final int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        }
        if (endIndex > this.len) {
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        }
        if (beginIndex > endIndex) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        }
        return new String(this.buffer, beginIndex, endIndex - beginIndex);
    }

    /**
     * Returns a substring of this buffer with leading and trailing whitespace
     * omitted. The substring begins with the first non-whitespace character
     * from {@code beginIndex} and extends to the last
     * non-whitespace character with the index lesser than
     * {@code endIndex}.
     *
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @return     the specified substring.
     * @exception  IndexOutOfBoundsException  if the
     *             {@code beginIndex} is negative, or
     *             {@code endIndex} is larger than the length of this
     *             buffer, or {@code beginIndex} is larger than
     *             {@code endIndex}.
     */
    public String substringTrimmed(final int beginIndex, final int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        }
        if (endIndex > this.len) {
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        }
        if (beginIndex > endIndex) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        }
        int beginIndex0 = beginIndex;
        int endIndex0 = endIndex;
        while (beginIndex0 < endIndex && HTTP.isWhitespace(this.buffer[beginIndex0])) {
            beginIndex0++;
        }
        while (endIndex0 > beginIndex0 && HTTP.isWhitespace(this.buffer[endIndex0 - 1])) {
            endIndex0--;
        }
        return new String(this.buffer, beginIndex0, endIndex0 - beginIndex0);
    }

    /**
     * {@inheritDoc}
     * @since 4.4
     */
    @Override
    public CharSequence subSequence(final int beginIndex, final int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        }
        if (endIndex > this.len) {
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        }
        if (beginIndex > endIndex) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        }
        return CharBuffer.wrap(this.buffer, beginIndex, endIndex);
    }

    @Override
    public String toString() {
        return new String(this.buffer, 0, this.len);
    }

}
