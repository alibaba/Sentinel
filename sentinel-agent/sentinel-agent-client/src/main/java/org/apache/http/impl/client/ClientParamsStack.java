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

import org.apache.http.params.AbstractHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/**
 * Represents a stack of parameter collections.
 * When retrieving a parameter, the stack is searched in a fixed order
 * and the first match returned. Setting parameters via the stack is
 * not supported. To minimize overhead, the stack has a fixed size and
 * does not maintain an internal array.
 * The supported stack entries, sorted by increasing priority, are:
 * <ol>
 * <li>Application parameters:
 *     expected to be the same for all clients used by an application.
 *     These provide "global", that is application-wide, defaults.
 *     </li>
 * <li>Client parameters:
 *     specific to an instance of
 *     {@link org.apache.http.client.HttpClient HttpClient}.
 *     These provide client specific defaults.
 *     </li>
 * <li>Request parameters:
 *     specific to a single request execution.
 *     For overriding client and global defaults.
 *     </li>
 * <li>Override parameters:
 *     specific to an instance of
 *     {@link org.apache.http.client.HttpClient HttpClient}.
 *     These can be used to set parameters that cannot be overridden
 *     on a per-request basis.
 *     </li>
 * </ol>
 * Each stack entry may be {@code null}. That is preferable over
 * an empty params collection, since it avoids searching the empty collection
 * when looking up parameters.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public class ClientParamsStack extends AbstractHttpParams {

    /** The application parameter collection, or {@code null}. */
    protected final HttpParams applicationParams;

    /** The client parameter collection, or {@code null}. */
    protected final HttpParams clientParams;

    /** The request parameter collection, or {@code null}. */
    protected final HttpParams requestParams;

    /** The override parameter collection, or {@code null}. */
    protected final HttpParams overrideParams;


    /**
     * Creates a new parameter stack from elements.
     * The arguments will be stored as-is, there is no copying to
     * prevent modification.
     *
     * @param aparams   application parameters, or {@code null}
     * @param cparams   client parameters, or {@code null}
     * @param rparams   request parameters, or {@code null}
     * @param oparams   override parameters, or {@code null}
     */
    public ClientParamsStack(final HttpParams aparams, final HttpParams cparams,
                             final HttpParams rparams, final HttpParams oparams) {
        applicationParams = aparams;
        clientParams      = cparams;
        requestParams     = rparams;
        overrideParams    = oparams;
    }


    /**
     * Creates a copy of a parameter stack.
     * The new stack will have the exact same entries as the argument stack.
     * There is no copying of parameters.
     *
     * @param stack     the stack to copy
     */
    public ClientParamsStack(final ClientParamsStack stack) {
        this(stack.getApplicationParams(),
             stack.getClientParams(),
             stack.getRequestParams(),
             stack.getOverrideParams());
    }


    /**
     * Creates a modified copy of a parameter stack.
     * The new stack will contain the explicitly passed elements.
     * For elements where the explicit argument is {@code null},
     * the corresponding element from the argument stack is used.
     * There is no copying of parameters.
     *
     * @param stack     the stack to modify
     * @param aparams   application parameters, or {@code null}
     * @param cparams   client parameters, or {@code null}
     * @param rparams   request parameters, or {@code null}
     * @param oparams   override parameters, or {@code null}
     */
    public ClientParamsStack(final ClientParamsStack stack,
                             final HttpParams aparams, final HttpParams cparams,
                             final HttpParams rparams, final HttpParams oparams) {
        this((aparams != null) ? aparams : stack.getApplicationParams(),
             (cparams != null) ? cparams : stack.getClientParams(),
             (rparams != null) ? rparams : stack.getRequestParams(),
             (oparams != null) ? oparams : stack.getOverrideParams());
    }


    /**
     * Obtains the application parameters of this stack.
     *
     * @return  the application parameters, or {@code null}
     */
    public final HttpParams getApplicationParams() {
        return applicationParams;
    }

    /**
     * Obtains the client parameters of this stack.
     *
     * @return  the client parameters, or {@code null}
     */
    public final HttpParams getClientParams() {
        return clientParams;
    }

    /**
     * Obtains the request parameters of this stack.
     *
     * @return  the request parameters, or {@code null}
     */
    public final HttpParams getRequestParams() {
        return requestParams;
    }

    /**
     * Obtains the override parameters of this stack.
     *
     * @return  the override parameters, or {@code null}
     */
    public final HttpParams getOverrideParams() {
        return overrideParams;
    }


    /**
     * Obtains a parameter from this stack.
     * See class comment for search order.
     *
     * @param name      the name of the parameter to obtain
     *
     * @return  the highest-priority value for that parameter, or
     *          {@code null} if it is not set anywhere in this stack
     */
    @Override
    public Object getParameter(final String name) {
        Args.notNull(name, "Parameter name");

        Object result = null;

        if (overrideParams != null) {
            result = overrideParams.getParameter(name);
        }
        if ((result == null) && (requestParams != null)) {
            result = requestParams.getParameter(name);
        }
        if ((result == null) && (clientParams != null)) {
            result = clientParams.getParameter(name);
        }
        if ((result == null) && (applicationParams != null)) {
            result = applicationParams.getParameter(name);
        }
        return result;
    }

    /**
     * Does <i>not</i> set a parameter.
     * Parameter stacks are read-only. It is possible, though discouraged,
     * to access and modify specific stack entries.
     * Derived classes may change this behavior.
     *
     * @param name      ignored
     * @param value     ignored
     *
     * @return  nothing
     *
     * @throws UnsupportedOperationException    always
     */
    @Override
    public HttpParams setParameter(final String name, final Object value)
        throws UnsupportedOperationException {

        throw new UnsupportedOperationException
            ("Setting parameters in a stack is not supported.");
    }


    /**
     * Does <i>not</i> remove a parameter.
     * Parameter stacks are read-only. It is possible, though discouraged,
     * to access and modify specific stack entries.
     * Derived classes may change this behavior.
     *
     * @param name      ignored
     *
     * @return  nothing
     *
     * @throws UnsupportedOperationException    always
     */
    @Override
    public boolean removeParameter(final String name) {
        throw new UnsupportedOperationException
        ("Removing parameters in a stack is not supported.");
    }


    /**
     * Does <i>not</i> copy parameters.
     * Parameter stacks are lightweight objects, expected to be instantiated
     * as needed and to be used only in a very specific context. On top of
     * that, they are read-only. The typical copy operation to prevent
     * accidental modification of parameters passed by the application to
     * a framework object is therefore pointless and disabled.
     * Create a new stack if you really need a copy.
     * <p>
     * Derived classes may change this behavior.
     * </p>
     *
     * @return {@code this} parameter stack
     */
    @Override
    public HttpParams copy() {
        return this;
    }


}
