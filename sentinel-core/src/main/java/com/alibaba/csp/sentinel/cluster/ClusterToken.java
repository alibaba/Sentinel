package com.alibaba.csp.sentinel.cluster;

/**
 * cluster token instance for distributed flow control.
 * it has two sub interfaces:
 * {@link com.alibaba.csp.sentinel.cluster.client.ClusterTokenClient}
 * {@link com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer}
 * @author houyi
 * @since 1.4.0
 **/
public interface ClusterToken {

    /**
     * Start the token instance.
     *
     * @throws Exception some error occurs
     */
    void start() throws Exception;

    /**
     * Stop the token instance.
     *
     * @throws Exception some error occurs
     */
    void stop() throws Exception;

}
