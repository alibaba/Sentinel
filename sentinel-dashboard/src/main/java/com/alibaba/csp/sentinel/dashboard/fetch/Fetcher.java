package com.alibaba.csp.sentinel.dashboard.fetch;

import java.util.List;

/**
 * Obtain Service Interface
 *
 * @author longqiang
 */
public interface Fetcher<T> {

    /**
     * fetch rules
     *
     * @param app
     * @param ip
     * @param port
     * @return boolean
     */
    List<T> fetch(String app, String ip, int port);

}
