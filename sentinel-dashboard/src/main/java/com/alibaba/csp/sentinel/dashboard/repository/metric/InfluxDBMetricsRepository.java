package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.client.InfluxDbUtils;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricPO;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InfluxDBMetricsRepository  implements MetricsRepository<MetricEntity> {
    /**时间格式*/
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**数据库名称*/
    private static final String SENTINEL_DATABASE = "sentinel_db";

    /**数据表名称*/
    private static final String METRIC_MEASUREMENT = "sentinel_metric";

    /**北京时间领先UTC时间8小时 UTC: Universal Time Coordinated,世界统一时间*/
    private static final Integer UTC_8 = 8;

    @Autowired
    InfluxDbUtils influxDbUtils;

    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }

        influxDbUtils.insert(SENTINEL_DATABASE, new InfluxDbUtils.InfluxDBInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDB influxDB) {
                if (metric.getId() == null) {
                    metric.setId(System.currentTimeMillis());
                }
                influxDB.setDatabase(database);
                doSave(influxDB, metric);
            }
        });
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }

        Iterator<MetricEntity> iterator = metrics.iterator();
        boolean next = iterator.hasNext();
        if (!next) {
            return;
        }

        influxDbUtils.insert(SENTINEL_DATABASE, new InfluxDbUtils.InfluxDBInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDB influxDB) {
                influxDB.setDatabase(database);
                while (iterator.hasNext()) {
                    MetricEntity metric = iterator.next();
                    if (metric.getId() == null) {
                        metric.setId(System.currentTimeMillis());
                    }
                    doSave(influxDB, metric);
                }
            }
        });
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<MetricEntity>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        if (StringUtil.isBlank(resource)) {
            return results;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM " + METRIC_MEASUREMENT);
        sql.append(" WHERE app=$app");
        sql.append(" AND resource=$resource");
        sql.append(" AND time>=$startTime");
        sql.append(" AND time<=$endTime");

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("app", app);
        paramMap.put("resource", resource);
        paramMap.put("startTime",startTime*1000000);
        paramMap.put("endTime",endTime*1000000);

        List<MetricPO> metricPOS = influxDbUtils.queryList(SENTINEL_DATABASE, sql.toString(), paramMap, MetricPO.class);
        if (CollectionUtils.isEmpty(metricPOS)) {
            return results;
        }

        return metricPOS.stream().map(MetricPO::copyOf).collect(Collectors.toList());
    }

    @Override
    public List<String> listResourcesOfApp(String app, long startTime) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM " + METRIC_MEASUREMENT);
        sql.append(" WHERE app=$app");
        sql.append(" AND time>=$startTime");

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("app", app);
        paramMap.put("startTime",startTime);

        List<MetricPO> metricEntities = influxDbUtils.queryList(SENTINEL_DATABASE, sql.toString(), paramMap, MetricPO.class);

        if (CollectionUtils.isEmpty(metricEntities)) {
            return results;
        }
        Map<String, MetricPO> resourceCount = new HashMap<>(32);

        for (MetricPO metricEntity : metricEntities) {
            String resource = metricEntity.getResource();
            if (resourceCount.containsKey(resource)) {
                MetricPO oldEntity = resourceCount.get(resource);
                oldEntity.addPassQps(metricEntity.getPassQps());
                oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
                oldEntity.addBlockQps(metricEntity.getBlockQps());
                oldEntity.addExceptionQps(metricEntity.getExceptionQps());
                oldEntity.addCount(1);
            } else {
                resourceCount.put(resource,metricEntity);
            }
        }

        // Order by last minute b_qps DESC.
        return resourceCount.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    MetricPO e1 = o1.getValue();
                    MetricPO e2 = o2.getValue();
                    int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                    if (t != 0) {
                        return t;
                    }
                    return e2.getPassQps().compareTo(e1.getPassQps());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void doSave(InfluxDB influxDB, MetricEntity metric) {
        influxDB.write(Point.measurement(METRIC_MEASUREMENT)
                .time(metric.getTimestamp().getTime(), TimeUnit.MILLISECONDS)// 因InfluxDB默认UTC时间，按北京时间算写入数据加8小时
                .tag("app", metric.getApp())
                .tag("resource", metric.getResource())
                .addField("id", metric.getId())
                .addField("gmtCreate", metric.getGmtCreate().getTime())
                .addField("gmtModified", metric.getGmtModified().getTime())
                .addField("passQps", metric.getPassQps())
                .addField("successQps", metric.getSuccessQps())
                .addField("blockQps", metric.getBlockQps())
                .addField("exceptionQps", metric.getExceptionQps())
                .addField("rt", metric.getRt())
                .addField("count", metric.getCount())
                .addField("resourceCode", metric.getResourceCode())
                .build());
    }
}
