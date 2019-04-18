package com.alibaba.csp.sentinel.dashboard.transpot.fetch;

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
     * @param app project name
     * @param ip project deployment IP
     * @param port project deployment port
     * @return boolean
     */
    List<T> fetch(String app, String ip, int port);

}
