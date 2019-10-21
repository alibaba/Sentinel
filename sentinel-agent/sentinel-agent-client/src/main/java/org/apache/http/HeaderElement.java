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

/**
 * One element of an HTTP {@link Header header} value consisting of
 * a name / value pair and a number of optional name / value parameters.
 * <p>
 * Some HTTP headers (such as the set-cookie header) have values that
 * can be decomposed into multiple elements.  Such headers must be in the
 * following form:
 * </p>
 * <pre>
 * header  = [ element ] *( "," [ element ] )
 * element = name [ "=" [ value ] ] *( ";" [ param ] )
 * param   = name [ "=" [ value ] ]
 *
 * name    = token
 * value   = ( token | quoted-string )
 *
 * token         = 1*&lt;any char except "=", ",", ";", &lt;"&gt; and
 *                       white space&gt;
 * quoted-string = &lt;"&gt; *( text | quoted-char ) &lt;"&gt;
 * text          = any char except &lt;"&gt;
 * quoted-char   = "\" char
 * </pre>
 * <p>
 * Any amount of white space is allowed between any part of the
 * header, element or param and is ignored. A missing value in any
 * element or param will be stored as the empty {@link String};
 * if the "=" is also missing <var>null</var> will be stored instead.
 *
 * @since 4.0
 */
public interface HeaderElement {

    /**
     * Returns header element name.
     *
     * @return header element name
     */
    String getName();

    /**
     * Returns header element value.
     *
     * @return header element value
     */
    String getValue();

    /**
     * Returns an array of name / value pairs.
     *
     * @return array of name / value pairs
     */
    NameValuePair[] getParameters();

    /**
     * Returns the first parameter with the given name.
     *
     * @param name parameter name
     *
     * @return name / value pair
     */
    NameValuePair getParameterByName(String name);

    /**
     * Returns the total count of parameters.
     *
     * @return parameter count
     */
    int getParameterCount();

    /**
     * Returns parameter with the given index.
     *
     * @param index index
     * @return name / value pair
     */
    NameValuePair getParameter(int index);

}

