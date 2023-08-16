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
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.cds.AbstractCdsFiliter;
import com.alibaba.csp.sentinel.datasource.xds.client.protocol.AbstractXdsProtocol;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lwj
 * @since 2.0.0
 */
public class CdsProtocol extends AbstractXdsProtocol<Cluster, AbstractCdsFiliter> {

    public static final Logger log = LoggerFactory.getLogger(CdsProtocol.class);

    public CdsProtocol(XdsConfigProperties xdsConfigProperties) {
        super(xdsConfigProperties);
    }

    @Override
    public Set<String> resolveResourceNames(List<Cluster> resources) {
        Set<String> endpoints = new HashSet<>();
        if (resources == null) {
            return endpoints;
        }
        for (Cluster cluster : resources) {
            cluster.getEdsClusterConfig().getServiceName();
            endpoints.add(cluster.getEdsClusterConfig().getServiceName());
        }
        return endpoints;
    }

    @Override
    public XdsUrlType getTypeUrl() {
        return XdsUrlType.CDS_URL;
    }

}
