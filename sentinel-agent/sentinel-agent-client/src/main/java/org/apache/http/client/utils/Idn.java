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
package org.apache.http.client.utils;

/**
 * Abstraction of international domain name (IDN) conversion.
 *
 * @deprecated (4.4) use standard {@link java.net.IDN}.
 *
 * @since 4.0
 */
@Deprecated
public interface Idn {
    /**
     * Converts a name from its punycode representation to Unicode.
     * The name may be a single hostname or a dot-separated qualified domain name.
     * @param punycode the Punycode representation
     * @return the Unicode domain name
     */
    String toUnicode(String punycode);
}
