package com.alibaba.csp.sentinel.dashboard.transpot.publish;

/**
 * Publishing Service Interface
 *
 * @author longqiang
 */
public interface Publisher<T> {

    /**
     * publish rules
     *
     * @param app project name
     * @param ip project deployment IP
     * @param port project deployment port
     * @return boolean
     */
    boolean publish(String app, String ip, int port);

}
