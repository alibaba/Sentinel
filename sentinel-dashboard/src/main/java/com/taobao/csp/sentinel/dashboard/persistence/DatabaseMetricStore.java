package com.taobao.csp.sentinel.dashboard.persistence;

import com.taobao.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Leo.yy   Created on 2018/8/8.
 * @description
 * @see
 */
@Component
public class DatabaseMetricStore {

    @Resource
    private MetricDAO metricDAO;

    public void saveAll(List<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }

        metricDAO.batchInsert(metrics);
    }

    public synchronized List<MetricEntity> queryByAppAndResouce(String app,
                                                                String resource,
                                                                Date startTime,
                                                                Date endTime) {

        return metricDAO.queryByAppAndResouce(app, resource, startTime, endTime);
    }

}
