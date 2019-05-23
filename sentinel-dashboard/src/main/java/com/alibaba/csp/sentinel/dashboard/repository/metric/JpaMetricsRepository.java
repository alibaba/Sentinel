package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.jpa.MetricPO;
import com.alibaba.csp.sentinel.util.StringUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * @author cdfive
 * @date 2018-09-17
 */
@Primary
@Transactional
@Repository("jpaMetricsRepository")
public class JpaMetricsRepository implements MetricsRepository<MetricEntity> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }

        MetricPO metricPO = new MetricPO();
        BeanUtils.copyProperties(metric, metricPO);
        em.persist(metricPO);
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }

        metrics.forEach(this::save);
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

        StringBuilder hql = new StringBuilder();
        hql.append("FROM MetricPO");
        hql.append(" WHERE app=:app");
        hql.append(" AND resource=:resource");
        hql.append(" AND timestamp>=:startTime");
        hql.append(" AND timestamp<=:endTime");

        Query query = em.createQuery(hql.toString());
        query.setParameter("app", app);
        query.setParameter("resource", resource);
        query.setParameter("startTime", Date.from(Instant.ofEpochMilli(startTime)));
        query.setParameter("endTime", Date.from(Instant.ofEpochMilli(endTime)));

        List<MetricPO> metricPOs = query.getResultList();
        if (CollectionUtils.isEmpty(metricPOs)) {
            return results;
        }

        for (MetricPO metricPO : metricPOs) {
            MetricEntity metricEntity = new MetricEntity();
            BeanUtils.copyProperties(metricPO, metricEntity);
            results.add(metricEntity);
        }

        return results;
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        StringBuilder hql = new StringBuilder();
        hql.append("FROM MetricPO");
        hql.append(" WHERE app=:app");
        hql.append(" AND timestamp>=:startTime");

        long startTime = System.currentTimeMillis() - 1000 * 60;
        Query query = em.createQuery(hql.toString());
        query.setParameter("app", app);
        query.setParameter("startTime", Date.from(Instant.ofEpochMilli(startTime)));

        List<MetricPO> metricPOs = query.getResultList();
        if (CollectionUtils.isEmpty(metricPOs)) {
            return results;
        }

        List<MetricEntity> metricEntities = new ArrayList<MetricEntity>();
        for (MetricPO metricPO : metricPOs) {
            MetricEntity metricEntity = new MetricEntity();
            BeanUtils.copyProperties(metricPO, metricEntity);
            metricEntities.add(metricEntity);
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
}