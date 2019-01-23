package com.alibaba.csp.sentinel.cluster;

/**
 * A cluster transport which can be extends by cluster client and cluster server
 * The duty of the {@link ClusterTransport} is to start or stop a cluster transport
 * It has two sub interfaces:
 * {@link com.alibaba.csp.sentinel.cluster.ClusterTransportClient}
 * {@link com.alibaba.csp.sentinel.cluster.ClusterTransportServer}
 *
 *
 * @author houyi
 * @since 1.4.0
 **/
public interface ClusterTransport {

    /**
     * Start the transport.
     *
     * @throws Exception some error occurred (e.g. initialization failed)
     */
    void start() throws Exception;

    /**
     * Stop the transport.
     *
     * @throws Exception some error occurred (e.g. shutdown failed)
     */
    void stop() throws Exception;

}
