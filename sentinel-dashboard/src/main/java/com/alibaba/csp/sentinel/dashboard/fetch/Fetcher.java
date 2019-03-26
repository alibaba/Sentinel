package com.alibaba.csp.sentinel.dashboard.fetch;

import java.util.List;

/**
 * Obtain Service Interface
 *
 * @author longqiang
 */
public interface Fetcher<T> {

    /**
     * 获取规则
     * @param app
     * @param ip
     * @param port
     * @return boolean
     * @throws
     * @author longqiang
     * @date 2019/3/18 11:19
     */
    List<T> fetch(String app, String ip, int port);

}
