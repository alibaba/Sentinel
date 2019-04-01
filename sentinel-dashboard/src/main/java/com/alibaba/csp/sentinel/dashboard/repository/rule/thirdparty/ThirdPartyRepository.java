package com.alibaba.csp.sentinel.dashboard.repository.rule.thirdparty;

/**
 * Providing third-party data storage services
 *
 * @author longqiang
 */
public interface ThirdPartyRepository<T> {

    /**
     * save data for third-party DataSource
     *
     * @param entity
     * @return T
     */
    T save(T entity);

    /**
     * delete data for third-party DataSource
     *
     * @param entity
     * @return T
     */
    T delete(T entity);

}
