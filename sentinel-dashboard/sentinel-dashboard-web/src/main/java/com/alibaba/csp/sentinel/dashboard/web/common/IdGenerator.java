package com.alibaba.csp.sentinel.dashboard.web.common;

/**
 * @author cdfive
 */
public interface IdGenerator {

    Long nextLongId();

    String nextStringId();
}
