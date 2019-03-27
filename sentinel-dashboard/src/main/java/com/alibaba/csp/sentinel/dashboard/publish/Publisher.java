package com.alibaba.csp.sentinel.dashboard.publish;

/**
 * Publishing Service Interface
 *
 * @author longqiang
 */
public interface Publisher<T> {

    /**
     * publish rules
     *
     * @param app
     * @param ip
     * @param port
     * @return boolean
     */
    boolean publish(String app, String ip, int port);

}
