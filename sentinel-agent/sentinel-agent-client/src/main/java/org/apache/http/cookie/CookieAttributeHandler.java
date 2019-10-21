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
package org.apache.http.cookie;

/**
 * This interface represents a cookie attribute handler responsible
 * for parsing, validating, and matching a specific cookie attribute,
 * such as path, domain, port, etc.
 *
 * Different cookie specifications can provide a specific
 * implementation for this class based on their cookie handling
 * rules.
 *
 *
 * @since 4.0
 */
public interface CookieAttributeHandler {

  /**
   * Parse the given cookie attribute value and update the corresponding
   * {@link org.apache.http.cookie.Cookie} property.
   *
   * @param cookie {@link org.apache.http.cookie.Cookie} to be updated
   * @param value cookie attribute value from the cookie response header
   */
  void parse(SetCookie cookie, String value)
          throws MalformedCookieException;

  /**
   * Peforms cookie validation for the given attribute value.
   *
   * @param cookie {@link org.apache.http.cookie.Cookie} to validate
   * @param origin the cookie source to validate against
   * @throws MalformedCookieException if cookie validation fails for this attribute
   */
  void validate(Cookie cookie, CookieOrigin origin)
          throws MalformedCookieException;

  /**
   * Matches the given value (property of the destination host where request is being
   * submitted) with the corresponding cookie attribute.
   *
   * @param cookie {@link org.apache.http.cookie.Cookie} to match
   * @param origin the cookie source to match against
   * @return {@code true} if the match is successful; {@code false} otherwise
   */
  boolean match(Cookie cookie, CookieOrigin origin);

}
