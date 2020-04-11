package com.alibaba.csp.sentinel.dashboard.service.impl.common.id;

/**
 * @author cdfive
 */
public interface IdGenerator {

    Long nextLongId();

    String nextStringId();
}
