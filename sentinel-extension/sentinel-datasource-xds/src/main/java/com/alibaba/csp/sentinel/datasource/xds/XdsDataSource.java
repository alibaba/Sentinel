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
package com.alibaba.csp.sentinel.datasource.xds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.xds.client.XdsClient;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.XdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds.AuthLdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds.TlsModeLdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.property.XdsProperty;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.TrustManager;

/**
 * @author lwj
 * @since 2.0.0
 */
public class XdsDataSource<Void> extends AbstractDataSource<XdsProperty, Void> {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean registerTrustManagerOnce = new AtomicBoolean(false);
    private XdsProperty xdsProperty;
    private XdsClient xdsClient;
    private XdsConfigProperties xdsConfigProperties;
    private List<XdsFilter> xdsFilterList = new ArrayList<>();

    public XdsDataSource(XdsConfigProperties xdsConfigProperties) {
        super(source -> null);
        this.xdsProperty = new XdsProperty();
        this.xdsConfigProperties = xdsConfigProperties;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            xdsClient = new XdsClient(xdsConfigProperties, xdsFilterList, xdsProperty.getCertPairRepository());
            xdsClient.start();
        } else {
            RecordLog.error("[XdsDataSource] XdsDataSource is running");
        }
    }

    public void registerTrustManager(TrustManager trustManager) {
        if (registerTrustManagerOnce.compareAndSet(false, true)) {
            AuthLdsFilter authLdsFilter = new AuthLdsFilter(xdsProperty.getAuthRepository());
            TlsModeLdsFilter tlsModeLdsFilter = new TlsModeLdsFilter(xdsProperty.getTlsModeRepository());
            xdsFilterList.add(authLdsFilter);
            xdsFilterList.add(tlsModeLdsFilter);
            xdsProperty.getCertPairRepository().registryRepositoryUpdateCallback(
                (cert) -> trustManager.storeCertPair(cert));
            xdsProperty.getAuthRepository().registryRepositoryUpdateCallback((rules) -> trustManager.storeRules(rules));
            xdsProperty.getTlsModeRepository().registryRepositoryUpdateCallback(
                (tlsMode) -> trustManager.storeTlsMode(tlsMode));
        } else {
            RecordLog.error("[XdsDataSource] TrustManager has already registered");
        }

    }

    @Override
    public Void loadConfig() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public XdsProperty readSource() throws Exception {
        return xdsProperty;
    }

    @Override
    public void close() throws Exception {
        if (running.compareAndSet(true, false)) {
            xdsClient.close();
        } else {
            RecordLog.error("[XdsDataSource] XdsDataSource is not running");
        }

    }
}
