package com.alibaba.csp.sentinel.dashboard.common;

/**
 * @author cdfive
 */
public interface IdGenerator {

    Long nextLongId();

    String nextStringId();
}
