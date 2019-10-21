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

package org.apache.http.impl.client;

import java.net.URI;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class represents a collection of {@link java.net.URI}s used
 * as redirect locations.
 *
 * @since 4.0
 */
public class RedirectLocations extends AbstractList<Object> {

    private final Set<URI> unique;
    private final List<URI> all;

    public RedirectLocations() {
        super();
        this.unique = new HashSet<URI>();
        this.all = new ArrayList<URI>();
    }

    /**
     * Test if the URI is present in the collection.
     */
    public boolean contains(final URI uri) {
        return this.unique.contains(uri);
    }

    /**
     * Adds a new URI to the collection.
     */
    public void add(final URI uri) {
        this.unique.add(uri);
        this.all.add(uri);
    }

    /**
     * Removes a URI from the collection.
     */
    public boolean remove(final URI uri) {
        final boolean removed = this.unique.remove(uri);
        if (removed) {
            final Iterator<URI> it = this.all.iterator();
            while (it.hasNext()) {
                final URI current = it.next();
                if (current.equals(uri)) {
                    it.remove();
                }
            }
        }
        return removed;
    }

    /**
     * Returns all redirect {@link URI}s in the order they were added to the collection.
     *
     * @return list of all URIs
     *
     * @since 4.1
     */
    public List<URI> getAll() {
        return new ArrayList<URI>(this.all);
    }

    /**
     * Returns the URI at the specified position in this list.
     *
     * @param index
     *            index of the location to return
     * @return the URI at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             {@code index &lt; 0 || index &gt;= size()})
     * @since 4.3
     */
    @Override
    public URI get(final int index) {
        return this.all.get(index);
    }

    /**
     * Returns the number of elements in this list. If this list contains more
     * than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this list
     * @since 4.3
     */
    @Override
    public int size() {
        return this.all.size();
    }

    /**
     * Replaces the URI at the specified position in this list with the
     * specified element (must be a URI).
     *
     * @param index
     *            index of the element to replace
     * @param element
     *            URI to be stored at the specified position
     * @return the URI previously at the specified position
     * @throws UnsupportedOperationException
     *             if the {@code set} operation is not supported by this list
     * @throws ClassCastException
     *             if the element is not a {@link URI}
     * @throws NullPointerException
     *             if the specified element is null and this list does not
     *             permit null elements
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             {@code index &lt; 0 || index &gt;= size()})
     * @since 4.3
     */
    @Override
    public Object set(final int index, final Object element) {
        final URI removed = this.all.set(index, (URI) element);
        this.unique.remove(removed);
        this.unique.add((URI) element);
        if (this.all.size() != this.unique.size()) {
            this.unique.addAll(this.all);
        }
        return removed;
    }

    /**
     * Inserts the specified element at the specified position in this list
     * (must be a URI). Shifts the URI currently at that position (if any) and
     * any subsequent URIs to the right (adds one to their indices).
     *
     * @param index
     *            index at which the specified element is to be inserted
     * @param element
     *            URI to be inserted
     * @throws UnsupportedOperationException
     *             if the {@code add} operation is not supported by this list
     * @throws ClassCastException
     *             if the element is not a {@link URI}
     * @throws NullPointerException
     *             if the specified element is null and this list does not
     *             permit null elements
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             {@code index &lt; 0 || index &gt; size()})
     * @since 4.3
     */
    @Override
    public void add(final int index, final Object element) {
        this.all.add(index, (URI) element);
        this.unique.add((URI) element);
    }

    /**
     * Removes the URI at the specified position in this list. Shifts any
     * subsequent URIs to the left (subtracts one from their indices). Returns
     * the URI that was removed from the list.
     *
     * @param index
     *            the index of the URI to be removed
     * @return the URI previously at the specified position
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             {@code index &lt; 0 || index &gt;= size()})
     * @since 4.3
     */
    @Override
    public URI remove(final int index) {
        final URI removed = this.all.remove(index);
        this.unique.remove(removed);
        if (this.all.size() != this.unique.size()) {
            this.unique.addAll(this.all);
        }
        return removed;
    }

    /**
     * Returns {@code true} if this collection contains the specified element.
     * More formally, returns {@code true} if and only if this collection
     * contains at least one element {@code e} such that
     * {@code (o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))}.
     *
     * @param o element whose presence in this collection is to be tested
     * @return {@code true} if this collection contains the specified
     *         element
     */
    @Override
    public boolean contains(final Object o) {
        return this.unique.contains(o);
    }

}
