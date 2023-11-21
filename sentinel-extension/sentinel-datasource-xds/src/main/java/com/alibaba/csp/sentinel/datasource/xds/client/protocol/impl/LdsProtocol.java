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
package com.alibaba.csp.sentinel.datasource.xds.client.protocol.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.csp.sentinel.datasource.xds.client.XdsUrlType;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds.AbstractLdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.client.protocol.AbstractXdsProtocol;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LdsProtocol contains information about authentication configuration
 *
 * @author lwj
 * @since 2.0.0
 */
public class LdsProtocol extends AbstractXdsProtocol<Listener, AbstractLdsFilter> {

    public static final Logger log = LoggerFactory.getLogger(LdsProtocol.class);

    public LdsProtocol(XdsConfigProperties xdsConfigProperties) {
        super(xdsConfigProperties);
    }

    @Override
    public Set<String> resolveResourceNames(List<Listener> resources) {
        Set<String> routeNames = new HashSet<>();
        for (Listener listener : resources) {
            for (FilterChain filterChain : listener.getFilterChainsList()) {
                for (Filter filter : filterChain.getFiltersList()) {
                    Any any = filter.getTypedConfig();
                    try {
                        if (!any.is(HttpConnectionManager.class)) {
                            continue;
                        }
                        HttpConnectionManager httpConnectionManager = any.unpack(HttpConnectionManager.class);
                        Rds rds = httpConnectionManager.getRds();
                        String routeConfigName = rds.getRouteConfigName();
                        if (StringUtil.isNotEmpty(routeConfigName)) {
                            routeNames.add(routeConfigName);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        continue;
                    }
                }
            }
        }
        return routeNames;
    }

    @Override
    public XdsUrlType getTypeUrl() {
        return XdsUrlType.LDS_URL;
    }

}
