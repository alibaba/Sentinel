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

package org.apache.http.params;

/**
 * HttpParams interface represents a collection of immutable values that define
 * a runtime behavior of a component. HTTP parameters should be simple objects:
 * integers, doubles, strings, collections and objects that remain immutable
 * at runtime. HttpParams is expected to be used in 'write once - read many' mode.
 * Once initialized, HTTP parameters are not expected to mutate in
 * the course of HTTP message processing.
 * <p>
 * The purpose of this interface is to define a behavior of other components.
 * Usually each complex component has its own HTTP parameter collection.
 * <p>
 * Instances of this interface can be linked together to form a hierarchy.
 * In the simplest form one set of parameters can use content of another one
 * to obtain default values of parameters not present in the local set.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public interface HttpParams {

    /**
     * Obtains the value of the given parameter.
     *
     * @param name the parent name.
     *
     * @return  an object that represents the value of the parameter,
     *          {@code null} if the parameter is not set or if it
     *          is explicitly set to {@code null}
     *
     * @see #setParameter(String, Object)
     */
    Object getParameter(String name);

    /**
     * Assigns the value to the parameter with the given name.
     *
     * @param name parameter name
     * @param value parameter value
     */
    HttpParams setParameter(String name, Object value);

    /**
     * Creates a copy of these parameters.
     *
     * @return  a new set of parameters holding the same values as this one
     */
    HttpParams copy();

    /**
     * Removes the parameter with the specified name.
     *
     * @param name parameter name
     *
     * @return true if the parameter existed and has been removed, false else.
     */
    boolean removeParameter(String name);

    /**
     * Returns a {@link Long} parameter value with the given name.
     * If the parameter is not explicitly set, the default value is returned.
     *
     * @param name the parent name.
     * @param defaultValue the default value.
     *
     * @return a {@link Long} that represents the value of the parameter.
     *
     * @see #setLongParameter(String, long)
     */
    long getLongParameter(String name, long defaultValue);

    /**
     * Assigns a {@link Long} to the parameter with the given name
     *
     * @param name parameter name
     * @param value parameter value
     */
    HttpParams setLongParameter(String name, long value);

    /**
     * Returns an {@link Integer} parameter value with the given name.
     * If the parameter is not explicitly set, the default value is returned.
     *
     * @param name the parent name.
     * @param defaultValue the default value.
     *
     * @return a {@link Integer} that represents the value of the parameter.
     *
     * @see #setIntParameter(String, int)
     */
    int getIntParameter(String name, int defaultValue);

    /**
     * Assigns an {@link Integer} to the parameter with the given name
     *
     * @param name parameter name
     * @param value parameter value
     */
    HttpParams setIntParameter(String name, int value);

    /**
     * Returns a {@link Double} parameter value with the given name.
     * If the parameter is not explicitly set, the default value is returned.
     *
     * @param name the parent name.
     * @param defaultValue the default value.
     *
     * @return a {@link Double} that represents the value of the parameter.
     *
     * @see #setDoubleParameter(String, double)
     */
    double getDoubleParameter(String name, double defaultValue);

    /**
     * Assigns a {@link Double} to the parameter with the given name
     *
     * @param name parameter name
     * @param value parameter value
     */
    HttpParams setDoubleParameter(String name, double value);

    /**
     * Returns a {@link Boolean} parameter value with the given name.
     * If the parameter is not explicitly set, the default value is returned.
     *
     * @param name the parent name.
     * @param defaultValue the default value.
     *
     * @return a {@link Boolean} that represents the value of the parameter.
     *
     * @see #setBooleanParameter(String, boolean)
     */
    boolean getBooleanParameter(String name, boolean defaultValue);

    /**
     * Assigns a {@link Boolean} to the parameter with the given name
     *
     * @param name parameter name
     * @param value parameter value
     */
    HttpParams setBooleanParameter(String name, boolean value);

    /**
     * Checks if a boolean parameter is set to {@code true}.
     *
     * @param name parameter name
     *
     * @return {@code true} if the parameter is set to value {@code true},
     *         {@code false} if it is not set or set to {@code false}
     */
    boolean isParameterTrue(String name);

    /**
     * Checks if a boolean parameter is not set or {@code false}.
     *
     * @param name parameter name
     *
     * @return {@code true} if the parameter is either not set or
     *         set to value {@code false},
     *         {@code false} if it is set to {@code true}
     */
    boolean isParameterFalse(String name);

}
