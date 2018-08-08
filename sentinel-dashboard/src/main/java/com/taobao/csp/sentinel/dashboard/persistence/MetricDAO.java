package com.taobao.csp.sentinel.dashboard.persistence;

import com.taobao.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author Leo.yy   Created on 2018/8/8.
 * @description
 * @see
 */
public interface MetricDAO {


    List<MetricEntity> queryByAppAndResouce(@Param("app") String app,
                                            @Param("resource") String resource,
                                            @Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime);


    int batchInsert(List<MetricEntity> entities);
}
