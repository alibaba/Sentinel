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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import com.alibaba.csp.sentinel.datasource.xds.client.cert.channel.XdsChannel;
import com.alibaba.csp.sentinel.datasource.xds.client.cert.manager.AbstractCertManager;
import com.alibaba.csp.sentinel.datasource.xds.client.cert.manager.IstioCertManager;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.XdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.client.protocol.AbstractXdsProtocol;
import com.alibaba.csp.sentinel.datasource.xds.client.protocol.PushMachine;
import com.alibaba.csp.sentinel.datasource.xds.client.protocol.impl.LdsProtocol;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.property.repository.CertPairRepository;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

/**
 * The Xds Client is used to send listen requests and accept all xds rules
 *
 * @author lwj
 * @since 2.0.0
 */
public class XdsClient {

    private final Map<String, AbstractXdsProtocol> protocolMap = new HashMap<>();
    private final Map<String, Set<String>> requestResource = new ConcurrentHashMap<>();
    private final PushMachine pushMachine;
    private final XdsConfigProperties xdsConfigProperties;
    private final XdsChannel xdsChannel;
    private final ScheduledExecutorService retry;
    private final AbstractCertManager abstractCertManager;
    private final List<XdsFilter> xdsFilters;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private CertPairRepository certPairRepository;
    private StreamObserver<DiscoveryRequest> requestObserver;
    private StreamObserver<DiscoveryResponse> responseObserver;

    public XdsClient(XdsConfigProperties xdsConfigProperties, List<XdsFilter> xdsFilters,
                     CertPairRepository certPairRepository) {
        this.certPairRepository = certPairRepository;
        this.abstractCertManager = new IstioCertManager(xdsConfigProperties, certPairRepository);
        this.xdsChannel = new XdsChannel(xdsConfigProperties, abstractCertManager);
        this.xdsConfigProperties = xdsConfigProperties;
        responseObserver = new XdsDiscoveryResponseObserver();
        this.requestObserver = createDiscoveryRequest(xdsChannel, responseObserver,
            xdsConfigProperties.getIstiodToken());
        this.retry = new ScheduledThreadPoolExecutor(1);
        //Currently only lds is supported
        LdsProtocol ldsProtocol = new LdsProtocol(xdsConfigProperties);
        protocolMap.put(ldsProtocol.getTypeUrl().getUrl(), ldsProtocol);
        this.pushMachine = new PushMachine(Arrays.asList(ldsProtocol), xdsConfigProperties);

        this.xdsFilters = xdsFilters;
    }

    private static StreamObserver<DiscoveryRequest> createDiscoveryRequest(XdsChannel xdsChannel,
                                                                           StreamObserver<DiscoveryResponse> observer,
                                                                           String istiodToken) {
        if (xdsChannel == null) {
            return null;
        }
        AggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceStub stub = AggregatedDiscoveryServiceGrpc
            .newStub(xdsChannel.getChannel());
        Metadata header = new Metadata();
        Metadata.Key<String> key = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, "Bearer " + istiodToken);
        stub = MetadataUtils.attachHeaders(stub, header);
        return stub.streamAggregatedResources(observer);
    }

    /**
     * Send observe request
     *
     * @param xdsUrlType
     * @param resourceNames
     */
    public void sendXdsRequest(XdsUrlType xdsUrlType, Set<String> resourceNames) {
        requestResource.put(xdsUrlType.getUrl(), resourceNames);
        DiscoveryRequest request = DiscoveryRequest.newBuilder()
            .setNode(xdsChannel.getNode()).setTypeUrl(xdsUrlType.getUrl())
            .addAllResourceNames(resourceNames).build();
        requestObserver.onNext(request);
    }

    /**
     * Return ack after receiving xds
     *
     * @param response
     */
    private void sendAckRequest(DiscoveryResponse response) {
        Set<String> ackResource = requestResource.get(response.getTypeUrl());
        if (ackResource == null) {
            ackResource = new HashSet<>();
        }
        DiscoveryRequest request = DiscoveryRequest.newBuilder()
            .setVersionInfo(response.getVersionInfo()).setNode(xdsChannel.getNode())
            .addAllResourceNames(ackResource).setTypeUrl(response.getTypeUrl())
            .setResponseNonce(response.getNonce()).build();
        requestObserver.onNext(request);
    }

    @PreDestroy
    public void close() {
        if (running.compareAndSet(true, false)) {
            retry.shutdown();
            abstractCertManager.close();
            xdsChannel.close();
        } else {
            RecordLog.error("[XdsDataSource] Xds client is not running");
        }

    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            for (AbstractXdsProtocol protocol : protocolMap.values()) {
                protocol.setInitStateMachine(this.pushMachine);
                protocol.setXdsClient(this);
            }
            for (XdsFilter xdsFilter : this.xdsFilters) {
                AbstractXdsProtocol protocol = protocolMap.get(xdsFilter.getXdsTypeUrl().getUrl());
                if (protocol == null) {
                    RecordLog.error("[XdsDataSource] Unsupported xdsFilter,xdsType={}", xdsFilter.getXdsTypeUrl());
                } else {
                    protocol.addFilters(xdsFilter);
                }
            }
            pushMachine.start();
            //Automatically restart when a new certificate is obtained
            certPairRepository.registryRepositoryUpdateCallback((certPair) -> {
                RecordLog.warn("[XdsDataSource] restart Channel because certPair update");
                restart();
            });
        } else {
            RecordLog.error("[XdsDataSource] Xds client is running");
        }

    }

    public void restart() {
        xdsChannel.restart();
        requestObserver = createDiscoveryRequest(xdsChannel, this.responseObserver,
            xdsConfigProperties.getIstiodToken());
        pushMachine.restart();
    }

    private class XdsDiscoveryResponseObserver implements StreamObserver<DiscoveryResponse> {

        @Override
        public void onNext(DiscoveryResponse discoveryResponse) {
            String typeUrl = discoveryResponse.getTypeUrl();
            RecordLog.info("[XdsDataSource] Receive notification from xds server, type: {}, size: {}", typeUrl,
                discoveryResponse.getResourcesCount());

            AbstractXdsProtocol protocol = protocolMap.get(typeUrl);
            if (protocol == null) {
                throw new UnsupportedOperationException("No protocol of type " + typeUrl);
            }

            List<?> responses = protocol.decodeXdsResponse(discoveryResponse);
            sendAckRequest(discoveryResponse);
            protocol.onResponseDecoded(responses);
        }

        @Override
        public void onError(Throwable throwable) {
            requestResource.clear();
            RecordLog.error("[XdsDataSource] Xds client reconnecting in a {} seconds",
                xdsConfigProperties.getReconnectionDelayS());
            retry.schedule(createRestartRunable(), xdsConfigProperties.getReconnectionDelayS(), TimeUnit.SECONDS);
        }

        @Override
        public void onCompleted() {
            RecordLog.info("[XdsDataSource] Xds client connect completed");
        }

        public Runnable createRestartRunable() {
            return () -> {
                try {
                    restart();
                    RecordLog.info("[XdsDataSource] Xds client reconnecting in a {} seconds",
                        xdsConfigProperties.getReconnectionDelayS());
                } catch (Exception e) {
                    RecordLog.error(
                        "[XdsDataSource] Xds client reconnecting error,the reconnection will continue in a {} seconds",
                        xdsConfigProperties.getReconnectionDelayS(), e);
                    retry.schedule(createRestartRunable(), xdsConfigProperties.getReconnectionDelayS(),
                        TimeUnit.SECONDS);
                }
            };
        }

    }

}
