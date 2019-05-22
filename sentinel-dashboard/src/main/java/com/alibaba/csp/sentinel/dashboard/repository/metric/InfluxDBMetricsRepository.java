package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.influxdb.MetricPO;
import com.alibaba.csp.sentinel.dashboard.util.InfluxDBUtils;
import com.alibaba.csp.sentinel.util.StringUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang.time.DateFormatUtils;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.WriteApi;
import org.influxdata.client.domain.WritePrecision;
import org.influxdata.client.write.Point;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * Metrics数据InfluxDB存储实现
 * </p>
 *
 * @author Leo
 * @date 2019-05-17 17:48
 */
@Repository("influxDBMetricsRepository")
public class InfluxDBMetricsRepository implements MetricsRepository<MetricEntity> {

    /** 时间格式 */
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /** 组织ID */
    private static final String ORG_ID = "03dba1e36bbc6000";
    /** 数据库名称 */
    private static final String BUCKET_NAME = "sentinel_metric_db";

    /** 数据表名称 */
    private static final String METRIC_MEASUREMENT = "sentinel_metric";

    /** 北京时间领先UTC时间8小时 UTC: Universal Time Coordinated,世界统一时间 */
    private static final Integer UTC_8 = 8;

    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }

        InfluxDBUtils.insert(BUCKET_NAME, new InfluxDBUtils.InfluxDBInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDBClient influxDB) {
                if (metric.getId() == null) {
                    metric.setId(System.currentTimeMillis());
                }
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

        InfluxDBUtils.insert(BUCKET_NAME, new InfluxDBUtils.InfluxDBInsertCallback() {
            @Override
            public void doCallBack(String bucket, InfluxDBClient influxDB) {
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
        sql.append("from(bucket: \"" + BUCKET_NAME + "\")");
        sql.append(" |> range(start: " + DateFormatUtils.format(new Date(startTime), DATE_FORMAT_PATTERN)
                + ", stop: " + DateFormatUtils.format(new Date(endTime), DATE_FORMAT_PATTERN) + ")");
        sql.append(" |> filter(fn: (r) => r._measurement == \"" + METRIC_MEASUREMENT + "\")");
        sql.append(" |> filter(fn: (r) => r.app == " + app + ")");
        sql.append(" |> filter(fn: (r) => r.resource == " + resource + ")");

        List<MetricPO> metricPOS = InfluxDBUtils.queryList(ORG_ID, sql.toString(), MetricPO.class);

        if (CollectionUtils.isEmpty(metricPOS)) {
            return results;
        }

        for (MetricPO metricPO : metricPOS) {
            results.add(convertToMetricEntity(metricPO));
        }

        return results;
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("from(bucket: \"" + BUCKET_NAME + "\")");
        sql.append(" |> range(start: -1m)");
        sql.append(" |> filter(fn: (r) => r._measurement == \"" + METRIC_MEASUREMENT + "\")");
        sql.append(" |> filter(fn: (r) => r.app == \"" + app + "\")");

        List<MetricPO> metricPOS = InfluxDBUtils.queryList(ORG_ID, sql.toString(), MetricPO.class);

        if (CollectionUtils.isEmpty(metricPOS)) {
            return results;
        }

        List<MetricEntity> metricEntities = new ArrayList<MetricEntity>();
        for (MetricPO metricPO : metricPOS) {
            metricEntities.add(convertToMetricEntity(metricPO));
        }

        Map<String, MetricEntity> resourceCount = new HashMap<>(32);

        for (MetricEntity metricEntity : metricEntities) {
            String resource = metricEntity.getResource();
            if (resourceCount.containsKey(resource)) {
                MetricEntity oldEntity = resourceCount.get(resource);
                oldEntity.addPassQps(metricEntity.getPassQps());
                oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
                oldEntity.addBlockQps(metricEntity.getBlockQps());
                oldEntity.addExceptionQps(metricEntity.getExceptionQps());
                oldEntity.addCount(1);
            } else {
                resourceCount.put(resource, MetricEntity.copyOf(metricEntity));
            }
        }

        // Order by last minute b_qps DESC.
        return resourceCount.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    MetricEntity e1 = o1.getValue();
                    MetricEntity e2 = o2.getValue();
                    int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                    if (t != 0) {
                        return t;
                    }
                    return e2.getPassQps().compareTo(e1.getPassQps());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private MetricEntity convertToMetricEntity(MetricPO metricPO) {
        MetricEntity metricEntity = new MetricEntity();

        metricEntity.setId(metricPO.getId());
        metricEntity.setGmtCreate(new Date(metricPO.getGmtCreate()));
        metricEntity.setGmtModified(new Date(metricPO.getGmtModified()));
        metricEntity.setApp(metricPO.getApp());
        metricEntity.setTimestamp(Date.from(metricPO.getTime().minusMillis(TimeUnit.HOURS.toMillis(UTC_8))));// 查询数据减8小时
        metricEntity.setResource(metricPO.getResource());
        metricEntity.setPassQps(metricPO.getPassQps());
        metricEntity.setSuccessQps(metricPO.getSuccessQps());
        metricEntity.setBlockQps(metricPO.getBlockQps());
        metricEntity.setExceptionQps(metricPO.getExceptionQps());
        metricEntity.setRt(metricPO.getRt());
        metricEntity.setCount(metricPO.getCount());

        return metricEntity;
    }

    private void doSave(InfluxDBClient influxDB, MetricEntity metric) {
        try (WriteApi writeApi = influxDB.getWriteApi()) {
            Point point = Point.measurement(METRIC_MEASUREMENT)
                    .time(Instant.now().toEpochMilli(), WritePrecision.MS)
                    .addTag("app", metric.getApp())
                    .addTag("resource", metric.getResource())
                    .addField("id", metric.getId())
                    .addField("gmtCreate", metric.getGmtCreate().getTime())
                    .addField("gmtModified", metric.getGmtModified().getTime())
                    .addField("passQps", metric.getPassQps())
                    .addField("successQps", metric.getSuccessQps())
                    .addField("blockQps", metric.getBlockQps())
                    .addField("exceptionQps", metric.getExceptionQps())
                    .addField("rt", metric.getRt())
                    .addField("count", metric.getCount())
                    .addField("resourceCode", metric.getResourceCode());
            writeApi.writePoint(BUCKET_NAME, ORG_ID, point);
        }
    }
}