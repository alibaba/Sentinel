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
package com.alibaba.csp.sentinel.util.io;

import java.io.Serializable;
import java.io.Writer;

/**
 * Copy from apache commons-io.
 */
public class StringBuilderWriter extends Writer implements Serializable {

    private static final long serialVersionUID = -146927496096066153L;
    private final StringBuilder builder;

    /**
     * Constructs a new {@link StringBuilder} instance with default capacity.
     */
    public StringBuilderWriter() {
        this.builder = new StringBuilder();
    }

    /**
     * Constructs a new {@link StringBuilder} instance with the specified capacity.
     *
     * @param capacity The initial capacity of the underlying {@link StringBuilder}
     */
    public StringBuilderWriter(final int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    /**
     * Constructs a new instance with the specified {@link StringBuilder}.
     *
     * <p>If {@code builder} is null a new instance with default capacity will be created.</p>
     *
     * @param builder The String builder. May be null.
     */
    public StringBuilderWriter(final StringBuilder builder) {
        this.builder = builder != null ? builder : new StringBuilder();
    }

    /**
     * Appends a single character to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final char value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends a character sequence to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends a portion of a character sequence to the {@link StringBuilder}.
     *
     * @param value The character to append
     * @param start The index of the first character
     * @param end   The index of the last character + 1
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value, final int start, final int end) {
        builder.append(value, start, end);
        return this;
    }

    /**
     * Closing this writer has no effect.
     */
    @Override
    public void close() {
        // no-op
    }

    /**
     * Flushing this writer has no effect.
     */
    @Override
    public void flush() {
        // no-op
    }

    /**
     * Writes a String to the {@link StringBuilder}.
     *
     * @param value The value to write
     */
    @Override
    public void write(final String value) {
        if (value != null) {
            builder.append(value);
        }
    }

    /**
     * Writes a portion of a character array to the {@link StringBuilder}.
     *
     * @param value  The value to write
     * @param offset The index of the first character
     * @param length The number of characters to write
     */
    @Override
    public void write(final char[] value, final int offset, final int length) {
        if (value != null) {
            builder.append(value, offset, length);
        }
    }

    /**
     * Returns the underlying builder.
     *
     * @return The underlying builder
     */
    public StringBuilder getBuilder() {
        return builder;
    }

    /**
     * Returns {@link StringBuilder#toString()}.
     *
     * @return The contents of the String builder.
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}
