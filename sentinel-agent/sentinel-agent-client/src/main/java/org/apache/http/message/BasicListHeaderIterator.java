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

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/**
 * Implementation of a {@link HeaderIterator} based on a {@link List}.
 * For use by {@link HeaderGroup}.
 *
 * @since 4.0
 */
public class BasicListHeaderIterator implements HeaderIterator {

    /**
     * A list of headers to iterate over.
     * Not all elements of this array are necessarily part of the iteration.
     */
    protected final List<Header> allHeaders;


    /**
     * The position of the next header in {@link #allHeaders allHeaders}.
     * Negative if the iteration is over.
     */
    protected int currentIndex;


    /**
     * The position of the last returned header.
     * Negative if none has been returned so far.
     */
    protected int lastIndex;


    /**
     * The header name to filter by.
     * {@code null} to iterate over all headers in the array.
     */
    protected String headerName;



    /**
     * Creates a new header iterator.
     *
     * @param headers   a list of headers over which to iterate
     * @param name      the name of the headers over which to iterate, or
     *                  {@code null} for any
     */
    public BasicListHeaderIterator(final List<Header> headers, final String name) {
        super();
        this.allHeaders = Args.notNull(headers, "Header list");
        this.headerName = name;
        this.currentIndex = findNext(-1);
        this.lastIndex = -1;
    }


    /**
     * Determines the index of the next header.
     *
     * @param pos       one less than the index to consider first,
     *                  -1 to search for the first header
     *
     * @return  the index of the next header that matches the filter name,
     *          or negative if there are no more headers
     */
    protected int findNext(final int pos) {
        int from = pos;
        if (from < -1) {
            return -1;
        }

        final int to = this.allHeaders.size()-1;
        boolean found = false;
        while (!found && (from < to)) {
            from++;
            found = filterHeader(from);
        }
        return found ? from : -1;
    }


    /**
     * Checks whether a header is part of the iteration.
     *
     * @param index     the index of the header to check
     *
     * @return  {@code true} if the header should be part of the
     *          iteration, {@code false} to skip
     */
    protected boolean filterHeader(final int index) {
        if (this.headerName == null) {
            return true;
        }

        // non-header elements, including null, will trigger exceptions
        final String name = (this.allHeaders.get(index)).getName();

        return this.headerName.equalsIgnoreCase(name);
    }


    // non-javadoc, see interface HeaderIterator
    @Override
    public boolean hasNext() {
        return (this.currentIndex >= 0);
    }


    /**
     * Obtains the next header from this iteration.
     *
     * @return  the next header in this iteration
     *
     * @throws NoSuchElementException   if there are no more headers
     */
    @Override
    public Header nextHeader()
        throws NoSuchElementException {

        final int current = this.currentIndex;
        if (current < 0) {
            throw new NoSuchElementException("Iteration already finished.");
        }

        this.lastIndex    = current;
        this.currentIndex = findNext(current);

        return this.allHeaders.get(current);
    }


    /**
     * Returns the next header.
     * Same as {@link #nextHeader nextHeader}, but not type-safe.
     *
     * @return  the next header in this iteration
     *
     * @throws NoSuchElementException   if there are no more headers
     */
    @Override
    public final Object next()
        throws NoSuchElementException {
        return nextHeader();
    }


    /**
     * Removes the header that was returned last.
     */
    @Override
    public void remove()
        throws UnsupportedOperationException {
        Asserts.check(this.lastIndex >= 0, "No header to remove");
        this.allHeaders.remove(this.lastIndex);
        this.lastIndex = -1;
        this.currentIndex--; // adjust for the removed element
    }
}
