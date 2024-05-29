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
package com.alibaba.csp.sentinel.datasource.xds.client;

/**
 * @author lwj
 * @since 2.0.0
 */
public enum XdsUrlType {
    /**
     * Url of cds request.
     */
    CDS_URL("CDS_URL", "type.googleapis.com/envoy.config.cluster.v3.Cluster"),

    /**
     * Url of eds request.
     */
    EDS_URL("EDS_URL", "type.googleapis.com/envoy.config.endpoint.v3.ClusterLoadAssignment"),

    /**
     * Url of lds request.
     */
    LDS_URL("LDS_URL", "type.googleapis.com/envoy.config.listener.v3.Listener"),

    /**
     * Url of rds request.
     */
    RDS_URL("RDS_URL", "type.googleapis.com/envoy.config.route.v3.RouteConfiguration"),

    /**
     * Url of sds request.
     */
    SDS_URL("SDS_URL", "type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.Secret"),
    ;

    private String key;
    private String url;

    XdsUrlType(String key, String url) {
        this.key = key;
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }
}
