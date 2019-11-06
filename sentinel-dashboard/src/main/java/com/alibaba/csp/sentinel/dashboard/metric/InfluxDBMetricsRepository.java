package com.alibaba.csp.sentinel.dashboard.metric;

import com.alibaba.csp.sentinel.dashboard.controller.MetricController;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.influxdb.SentinelMetricDO;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import com.alibaba.csp.sentinel.dashboard.util.InfluxDBUtils;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dinglang
 * @since 2019-08-27 15:52
 */
@Repository("influxDBMetricsRepository")
public class InfluxDBMetricsRepository implements MetricsRepository<MetricEntity> {

    /**时间格式*/
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**数据库名称*/
    private static final String SENTINEL_DATABASE = "sentinel_db";

    /**数据表名称*/
    private static final String METRIC_MEASUREMENT = "sentinel_metric";

    /**北京时间领先UTC时间8小时 UTC: Universal Time Coordinated,世界统一时间*/
    private static final Integer UTC_8 = 8;

    private static Logger logger = LoggerFactory.getLogger(MetricController.class);


    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }

        InfluxDBUtils.insert(SENTINEL_DATABASE, (database, influxDB) -> {
            if (metric.getId() == null) {
                metric.setId(System.currentTimeMillis());
            }
            doSave(influxDB, metric);
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

        InfluxDBUtils.insert(SENTINEL_DATABASE, (database, influxDB) -> {
            while (iterator.hasNext()) {
                MetricEntity metric = iterator.next();
                if (metric.getId() == null) {
                    metric.setId(System.currentTimeMillis());
                }
                doSave(influxDB, metric);
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
        sql.append("  tz('Asia/Shanghai') ");
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("app", app);
        paramMap.put("resource", resource);
        paramMap.put("startTime", DateFormatUtils.format(new Date(startTime), DATE_FORMAT_PATTERN));
        paramMap.put("endTime", DateFormatUtils.format(new Date(endTime), DATE_FORMAT_PATTERN));

        List<SentinelMetricDO> SentinelMetricDOS = InfluxDBUtils.queryList(SENTINEL_DATABASE, sql.toString(), paramMap, SentinelMetricDO.class);

        if (CollectionUtils.isEmpty(SentinelMetricDOS)) {
            return results;
        }

        for (SentinelMetricDO SentinelMetricDO : SentinelMetricDOS) {
            results.add(convertToMetricEntity(SentinelMetricDO));
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
        sql.append("SELECT * FROM " + METRIC_MEASUREMENT);
        sql.append(" WHERE app=$app");
        sql.append(" AND time>=$startTime");
        sql.append("  tz('Asia/Shanghai') ");

        Map<String, Object> paramMap = new HashMap<String, Object>();
        long startTime = System.currentTimeMillis() - 1000 * 60;
        paramMap.put("app", app);
        paramMap.put("startTime", DateFormatUtils.format(new Date(startTime), DATE_FORMAT_PATTERN));

        List<SentinelMetricDO> SentinelMetricDOS = InfluxDBUtils.queryList(SENTINEL_DATABASE, sql.toString(), paramMap, SentinelMetricDO.class);

        if (CollectionUtils.isEmpty(SentinelMetricDOS)) {
            return results;
        }

        List<MetricEntity> metricEntities = new ArrayList<MetricEntity>();
        for (SentinelMetricDO SentinelMetricDO : SentinelMetricDOS) {
            metricEntities.add(convertToMetricEntity(SentinelMetricDO));
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

    private MetricEntity convertToMetricEntity(SentinelMetricDO SentinelMetricDO) {
        MetricEntity metricEntity = new MetricEntity();

        metricEntity.setId(SentinelMetricDO.getId());
        metricEntity.setGmtCreate(new Date(SentinelMetricDO.getGmtCreate()));
        metricEntity.setGmtModified(new Date(SentinelMetricDO.getGmtModified()));
        metricEntity.setApp(SentinelMetricDO.getApp());
        //metricEntity.setTimestamp(Date.from(SentinelMetricDO.getTime().minusMillis(TimeUnit.HOURS.toMillis(UTC_8))));// 查询数据减8小时
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss+08:00" );
        try {
            metricEntity.setTimestamp(sdf.parse(SentinelMetricDO.getTime()));
        } catch (ParseException e) {
        logger.error("时间序列转换出错:{}",SentinelMetricDO.getTime(),e);
        }
        metricEntity.setResource(SentinelMetricDO.getResource());
        metricEntity.setPassQps(SentinelMetricDO.getPassQps());
        metricEntity.setSuccessQps(SentinelMetricDO.getSuccessQps());
        metricEntity.setBlockQps(SentinelMetricDO.getBlockQps());
        metricEntity.setExceptionQps(SentinelMetricDO.getExceptionQps());
        metricEntity.setRt(SentinelMetricDO.getRt());
        metricEntity.setCount(SentinelMetricDO.getCount());

        return metricEntity;
    }

    private void doSave(InfluxDB influxDB, MetricEntity metric) {
        try {
            influxDB.write(Point.measurement(METRIC_MEASUREMENT)
                    //.time(DateUtils.addHours(metric.getTimestamp(), UTC_8).getTime(), TimeUnit.MILLISECONDS)// 因InfluxDB默认UTC时间，按北京时间算写入数据加8小时
                    .time(metric.getTimestamp().getTime(),TimeUnit.MILLISECONDS)
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
        } catch (Exception ex) {
            logger.error("写入sentinel_metric失败", ex);
        }
    }
}