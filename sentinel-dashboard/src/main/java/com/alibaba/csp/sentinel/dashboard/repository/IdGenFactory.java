package com.alibaba.csp.sentinel.dashboard.repository;

/**
 * @author FengJianxin
 * @since 1.8.6.2
 */
public class IdGenFactory {

    public static IdGen create(IdGenType type) {
        return type.getConstructor().get();
    }

}
