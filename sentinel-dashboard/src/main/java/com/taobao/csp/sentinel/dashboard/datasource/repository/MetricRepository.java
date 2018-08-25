package com.taobao.csp.sentinel.dashboard.datasource.repository;

import com.taobao.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author huyong
 */
public interface MetricRepository {

    /**
     * find the entity by condition
     * @param startTime
     * @param endTime
     * @param app
     * @param resource
     * @return
     */
    List<MetricEntity> findByResourceAndAppAndTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
            @Param("app") String app, @Param("resource") String resource);

    /**
     * find the entity by condition
     * @param startTime
     * @param endTime
     * @param app
     * @return
     */
    List<MetricEntity> findByResourceAndTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
            @Param("app") String app);

    /**
     * find the entity by condition
     * @param metrics
     */
    void save(@Param("metrics") Iterable<MetricEntity> metrics);
}
