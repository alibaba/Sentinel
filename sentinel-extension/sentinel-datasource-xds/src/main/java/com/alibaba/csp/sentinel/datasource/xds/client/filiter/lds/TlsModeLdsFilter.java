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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.datasource.xds.property.repository.TlsModeRepository;
import com.alibaba.csp.sentinel.trust.tls.TlsMode;
import com.alibaba.csp.sentinel.util.CollectionUtil;

import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.FilterChainMatch;
import io.envoyproxy.envoy.config.listener.v3.Listener;

/**
 * Filter for tls mode
 *
 * @author lwj
 * @since 2.0.0
 */

public class TlsModeLdsFilter extends AbstractLdsFilter {

    private TlsModeRepository tlsModeRepository;

    public TlsModeLdsFilter(TlsModeRepository tlsModeRepository) {
        this.tlsModeRepository = tlsModeRepository;
    }

    @Override
    public boolean resolve(List<Listener> listeners) {
        if (CollectionUtil.isEmpty(listeners)) {
            return false;
        }
        Map<Integer, TlsMode.TlsType> portToTls = new HashMap<>();
        for (Listener listener : listeners) {
            List<FilterChain> filterChains = listener.getFilterChainsList();
            if (!LDS_VIRTUAL_INBOUND.equals(listener.getName())) {
                continue;
            }
            if (CollectionUtil.isEmpty(filterChains)) {
                continue;
            }
            for (FilterChain filterChain : filterChains) {
                if (!LDS_VIRTUAL_INBOUND.equals(filterChain.getName())) {
                    continue;
                }
                FilterChainMatch match = filterChain.getFilterChainMatch();
                int port = match.getDestinationPort().getValue();

                TlsMode.TlsType newTlsType = TlsMode.TlsType.DISABLE;
                if (LDS_TLS.equals(match.getTransportProtocol())) {
                    newTlsType = TlsMode.TlsType.STRICT;
                }
                //PERMISSIVE mode resolves both plaintext and tls
                if (portToTls.containsKey(port)) {
                    newTlsType = TlsMode.TlsType.PERMISSIVE;
                }
                portToTls.put(port, newTlsType);
            }
        }
        TlsMode newTlsMode = new TlsMode();
        for (Map.Entry<Integer, TlsMode.TlsType> entry : portToTls.entrySet()) {
            int port = entry.getKey();
            //If the port is 0, the protocol is configured globally,
            //otherwise, the protocol is single-port
            TlsMode.TlsType tlsType = entry.getValue();
            if (0 == port) {
                newTlsMode.setGlobalTls(tlsType);
            } else {
                newTlsMode.setPortTls(port, tlsType);
            }
        }

        tlsModeRepository.update(newTlsMode);
        return true;
    }

}
