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
package com.alibaba.csp.sentinel.datasource.xds.client.cert.channel;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import com.alibaba.csp.sentinel.datasource.xds.client.cert.manager.AbstractCertManager;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.expection.XdsException;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.cert.CertPair;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Xds Channel
 *
 * @author lwj
 * @since 2.0.0
 */
public class XdsChannel implements AutoCloseable {

    public static final String SVC_CLUSTER_LOCAL = ".svc.cluster.local";
    private final XdsConfigProperties xdsConfigProperties;
    private final Node node;
    private final AbstractCertManager abstractCertManager;
    private ManagedChannel channel;

    public XdsChannel(XdsConfigProperties xdsConfigProperties,
                      AbstractCertManager abstractCertManager) {
        try {
            this.xdsConfigProperties = xdsConfigProperties;
            this.abstractCertManager = abstractCertManager;
            this.channel = createManagedChannel(abstractCertManager.getCertPair(), xdsConfigProperties.getHost(),
                xdsConfigProperties.getPort());
            this.node = createNode(xdsConfigProperties.getPodName(), xdsConfigProperties.getNamespace(),
                xdsConfigProperties.getClusterId());
        } catch (Exception e) {
            throw new XdsException("Init xds channel failed", e);
        }
    }

    /**
     * Create a Node that mimics envoy's behavior
     *
     * @param podName
     * @param namespace
     * @param clusterId
     * @return
     */
    public static Node createNode(String podName, String namespace, String clusterId) {
        Node node = null;
        try {
            String id = createNodeId(podName, namespace);
            Struct.Builder metaBuilder = Struct.newBuilder();
            /**
             * 1. When creating a rule, we must specify the namespace,
             * and istio will only push the corresponding xds to the pod of the specified namespace.
             * The namespace here informs istio of the namespace to which it belongs.
             *
             * 2. If the creation rule uses the namespace where istio resides (default is istio-system),
             * xds will be pushed to all namespaces.
             *
             * Priority: When rules 1 and 2 conflict, rule 1 takes effect
             */
            metaBuilder.putFields("NAMESPACE", Value.newBuilder().setStringValue(namespace).build());
            node = Node.newBuilder()
                .setId(id)
                .setCluster(clusterId).setMetadata(metaBuilder.build()).build();
            RecordLog.info("[XdsDataSource] Connect istio success,and podName={},namespace={},clusterId={}", podName,
                namespace, clusterId);
        } catch (Exception e) {
            throw new XdsException("Unable to create node for xds request", e);
        }
        return node;
    }

    /**
     * Id according to envoy standard
     * This id can be obtained by command
     * > istioctl pc bootstrap ${podName} -n ${namespace} -o json
     * <p>
     * There is usually a selector when the configuration is delivered
     * for example:
     * '''
     * spec:
     * <br> selector:
     * <br> <br> matchLabels:
     * <br> <br> <br> app: details
     * '''
     * Istio will select the appropriate pod push xds based on the selector.
     * PodName in Id uniquely identifies a pod.
     *
     * @param podName
     * @param namespace
     * @return
     */
    public static String createNodeId(String podName, String namespace) {
        String ip = "127.0.0.1";
        try {
            InetAddress local = InetAddress.getLocalHost();
            ip = local.getHostAddress();
        } catch (UnknownHostException e) {
            RecordLog.error("[XdsDataSource] Can not get local ip, and use default IP={}", ip, e);

        }
        return String.format("sidecar~%s~%s.%s~%s" + SVC_CLUSTER_LOCAL, ip, podName, namespace, namespace);
    }

    private static ManagedChannel createManagedChannel(CertPair certPair, String host, int port) throws SSLException {
        if (null == certPair || null == certPair.getRawCertificateChain() || null == certPair.getRawPrivateKey()) {
            throw new XdsException("Invalid certificate");
        }
        SslContext sslcontext = GrpcSslContexts.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .keyManager(
                new ByteArrayInputStream(certPair.getRawCertificateChain()),
                new ByteArrayInputStream(certPair.getRawPrivateKey()))
            .build();

        return NettyChannelBuilder
            .forTarget(host + ":" + port)
            .negotiationType(NegotiationType.TLS).sslContext(sslcontext).build();
    }

    public Node getNode() {
        return node;
    }

    public ManagedChannel getChannel() {
        return this.channel;
    }

    @Override
    public void close() {
        if (null != channel) {
            RecordLog.warn("[XdsDataSource] Xds channel closing!");
            channel.shutdown();
        }
    }

    public void restart() {
        try {
            close();
            this.channel = createManagedChannel(abstractCertManager.getCertPair(), xdsConfigProperties.getHost(),
                xdsConfigProperties.getPort());
            RecordLog.warn("[XdsDataSource] Restart XdsChannel");
        } catch (Exception e) {
            RecordLog.error("[XdsDataSource] Failed to restart XdsChannel", e);
        }
    }

}
