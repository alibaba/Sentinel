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

package org.apache.http;

import java.util.Iterator;

/**
 * A type-safe iterator for {@link HeaderElement} objects.
 *
 * @since 4.0
 */
public interface HeaderElementIterator extends Iterator<Object> {

    /**
     * Indicates whether there is another header element in this
     * iteration.
     *
     * @return  {@code true} if there is another header element,
     *          {@code false} otherwise
     */
    @Override
    boolean hasNext();

    /**
     * Obtains the next header element from this iteration.
     * This method should only be called while {@link #hasNext hasNext}
     * is true.
     *
     * @return  the next header element in this iteration
     */
    HeaderElement nextElement();

}
