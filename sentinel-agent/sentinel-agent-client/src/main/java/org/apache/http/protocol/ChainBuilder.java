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

package org.apache.http.protocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Builder class to build a linked list (chain) of unique class instances. Each class can have
 * only one instance in the list. Useful for building lists of protocol interceptors.
 *
 * @see ImmutableHttpProcessor
 *
 * @since 4.3
 */
final class ChainBuilder<E> {

    private final LinkedList<E> list;
    private final Map<Class<?>, E> uniqueClasses;

    public ChainBuilder() {
        this.list = new LinkedList<E>();
        this.uniqueClasses = new HashMap<Class<?>, E>();
    }

    private void ensureUnique(final E e) {
        final E previous = this.uniqueClasses.remove(e.getClass());
        if (previous != null) {
            this.list.remove(previous);
        }
        this.uniqueClasses.put(e.getClass(), e);
    }

    public ChainBuilder<E> addFirst(final E e) {
        if (e == null) {
            return this;
        }
        ensureUnique(e);
        this.list.addFirst(e);
        return this;
    }

    public ChainBuilder<E> addLast(final E e) {
        if (e == null) {
            return this;
        }
        ensureUnique(e);
        this.list.addLast(e);
        return this;
    }

    public ChainBuilder<E> addAllFirst(final Collection<E> c) {
        if (c == null) {
            return this;
        }
        for (final E e: c) {
            addFirst(e);
        }
        return this;
    }

    public ChainBuilder<E> addAllFirst(final E... c) {
        if (c == null) {
            return this;
        }
        for (final E e: c) {
            addFirst(e);
        }
        return this;
    }

    public ChainBuilder<E> addAllLast(final Collection<E> c) {
        if (c == null) {
            return this;
        }
        for (final E e: c) {
            addLast(e);
        }
        return this;
    }

    public ChainBuilder<E> addAllLast(final E... c) {
        if (c == null) {
            return this;
        }
        for (final E e: c) {
            addLast(e);
        }
        return this;
    }

    public LinkedList<E> build() {
        return new LinkedList<E>(this.list);
    }

}
