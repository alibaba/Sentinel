/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel.adapter.gateway.zuul.constants;

import com.netflix.zuul.ZuulFilter;

/**
 * @author tiger
 */
public class ZuulConstant {

    /**
     * Zuul {@link com.netflix.zuul.context.RequestContext} key for use in load balancer.
     */
    public static final String SERVICE_ID_KEY = "serviceId";
    /**
     * Zuul {@link com.netflix.zuul.context.RequestContext} key for proxying (route ID).
     */
    public static final String PROXY_ID_KEY = "proxy";

    /**
     * {@link ZuulFilter#filterType()} error type.
     */
    public static final String ERROR_TYPE = "error";

    /**
     * {@link ZuulFilter#filterType()} post type.
     */
    public static final String POST_TYPE = "post";

    /**
     * {@link ZuulFilter#filterType()} pre type.
     */
    public static final String PRE_TYPE = "pre";

    /**
     * {@link ZuulFilter#filterType()} route type.
     */
    public static final String ROUTE_TYPE = "route";

    /**
     * Filter Order for SEND_RESPONSE_FILTER_ORDER
     */
    public static final int SEND_RESPONSE_FILTER_ORDER = 1000;

    /**
     * Zuul use Sentinel as default context when serviceId is empty.
     */
    public static final String ZUUL_DEFAULT_CONTEXT = "zuul_default_context";

    /**
     * Zuul context key for keeping Sentinel entries.
     *
     * @since 1.6.0
     */
    public static final String ZUUL_CTX_SENTINEL_ENTRIES_KEY = "_sentinel_entries";

    private ZuulConstant(){}
}
