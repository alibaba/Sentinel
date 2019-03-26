package com.alibaba.csp.sentinel.dashboard.publish;

/**
 * Publishing Service Interface
 *
 * @author longqiang
 */
public interface Publisher<T> {

    /**
     * 发布规则
     * @param app
     * @param ip
     * @param port
     * @return boolean
     * @throws
     * @author longqiang
     * @date 2019/3/18 11:19
     */
    boolean publish(String app, String ip, int port);

}
