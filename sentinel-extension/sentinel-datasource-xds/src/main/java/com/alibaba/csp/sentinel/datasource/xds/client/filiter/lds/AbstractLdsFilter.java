/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds;

import java.util.List;

import com.alibaba.csp.sentinel.datasource.xds.client.XdsUrlType;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.XdsFilter;

import io.envoyproxy.envoy.config.listener.v3.Listener;

/**
 * @author lwj
 * @since 2.0.0
 */
public abstract class AbstractLdsFilter implements XdsFilter<List<Listener>> {

    protected static final String LDS_VIRTUAL_INBOUND = "virtualInbound";

    protected static final String LDS_TLS = "tls";

    protected static final String LDS_CONNECTION_MANAGER = "envoy.filters.network.http_connection_manager";

    protected static final String LDS_RBAC_FILTER = "envoy.filters.http.rbac";

    protected static final String LDS_JWT_FILTER = "envoy.filters.http.jwt_authn";

    protected static final String LDS_ISTIO_AUTHN = "istio_authn";

    protected static final String LDS_REQUEST_AUTH_PRINCIPAL = "request.auth.principal";

    protected static final String LDS_REQUEST_AUTH_AUDIENCE = "request.auth.audiences";

    protected static final String LDS_REQUEST_AUTH_PRESENTER = "request.auth.presenter";

    protected static final String LDS_REQUEST_AUTH_CLAIMS = "request.auth.claims";

    protected static final String LDS_HEADER_NAME_AUTHORITY = ":authority";

    protected static final String LDS_HEADER_NAME_METHOD = ":method";

    public XdsUrlType getXdsTypeUrl() {
        return XdsUrlType.LDS_URL;
    }

}
