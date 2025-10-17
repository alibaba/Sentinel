package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.config.metric.MetricsProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.NanoIdUtils;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
public class RedisMetricsRepository implements MetricsRepository<MetricEntity> {

    @Resource
    private MetricsProperties metricsProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String, MetricEntity> metricRedisTemplate;


    @SuppressWarnings({"unchecked"})
    @Override
    public void save(final MetricEntity metric) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisSerializer<MetricEntity> metricSerializer = (RedisSerializer<MetricEntity>) metricRedisTemplate.getHashValueSerializer();

        metricRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            saveMetric(connection, stringRedisSerializer, metricSerializer, metric);
            return null;
        });
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void saveAll(final Iterable<MetricEntity> metrics) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisSerializer<MetricEntity> metricSerializer = (RedisSerializer<MetricEntity>) metricRedisTemplate.getHashValueSerializer();

        metricRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (MetricEntity metric : metrics) {
                saveMetric(connection, stringRedisSerializer, metricSerializer, metric);
            }
            return null;
        });
    }

    @SuppressWarnings("DataFlowIssue")
    private void saveMetric(final RedisConnection connection,
                            final StringRedisSerializer stringRedisSerializer,
                            final RedisSerializer<MetricEntity> metricSerializer,
                            final MetricEntity metric) {

        final String app = metric.getApp();
        final String res = metric.getResource();
        final ValueOperations<String, MetricEntity> opsValue = metricRedisTemplate.opsForValue();
        final String lastMetricKey = buildResLastMetricKey(app, res);
        MetricEntity oldMetric = opsValue.get(lastMetricKey);
        if (oldMetric == null) {
            oldMetric = metric;
        } else {
            oldMetric.addPassQps(metric.getPassQps());
            oldMetric.addRtAndSuccessQps(metric.getRt(), metric.getSuccessQps());
            oldMetric.addBlockQps(metric.getBlockQps());
            oldMetric.addExceptionQps(metric.getExceptionQps());
            oldMetric.addCount(1);
        }

        final String id = genId();
        connection.multi();

        final long expireTime = metricsProperties.getMaxLiveTime().toMillis() / 1000 + 3;
        final byte[] dataKey = stringRedisSerializer.serialize(buildDataHashKey(app, res));
        connection.hSet(
                dataKey,
                stringRedisSerializer.serialize(id),
                metricSerializer.serialize(metric)
        );
        connection.expire(dataKey, expireTime);

        final byte[] tsKey = stringRedisSerializer.serialize(buildTsKey(app, res));
        connection.zAdd(
                tsKey,
                metric.getTimestamp().getTime(),
                stringRedisSerializer.serialize(id)
        );
        connection.expire(tsKey, expireTime);

        final byte[] resKey = stringRedisSerializer.serialize(buildResHashKey(app));
        connection.zAdd(
                resKey,
                metric.getTimestamp().getTime(),
                stringRedisSerializer.serialize(metric.getResource())
        );
        connection.expire(resKey, expireTime);

        final byte[] resMetricLastKey = stringRedisSerializer.serialize(lastMetricKey);
        final byte[] resMetricLastValue = metricSerializer.serialize(oldMetric);
        connection.set(resMetricLastKey, resMetricLastValue);
        connection.expire(resMetricLastKey, expireTime);
        connection.exec();
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(final String app, final String resource,
                                                           final long startTime, final long endTime) {
        final ZSetOperations<String, String> opsZSet = stringRedisTemplate.opsForZSet();
        final Set<String> range = opsZSet.rangeByScore(buildTsKey(app, resource), startTime, endTime);
        if (CollectionUtils.isEmpty(range)) {
            return Collections.emptyList();
        }
        final HashOperations<String, String, MetricEntity> opsHash = metricRedisTemplate.opsForHash();
        final List<MetricEntity> metricList = opsHash.multiGet(buildDataHashKey(app, resource), range);
        return Optional.of(metricList).orElseGet(Collections::emptyList);
    }

    @Override
    public List<String> listResourcesOfApp(final String app) {
        final List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        final Pair<Long, Long> maxLiveTime = getMaxLiveTime();
        final long startTime = maxLiveTime.getFirst();

        final ZSetOperations<String, String> opsZSet = stringRedisTemplate.opsForZSet();
        final ValueOperations<String, MetricEntity> opsValue = metricRedisTemplate.opsForValue();
        final String key = buildResHashKey(app);
        // remove resource where more than max time
        opsZSet.removeRangeByScore(key, 0, startTime);

        final Set<String> resSet = opsZSet.range(key, 0, -1);
        if (CollectionUtils.isEmpty(resSet)) {
            return Collections.emptyList();
        }
        final List<String> scoreKeys = resSet.stream().map(item -> buildResLastMetricKey(app, item)).collect(Collectors.toList());
        final List<MetricEntity> metrics = opsValue.multiGet(scoreKeys);
        Map<String, MetricEntity> resourceCount = new LinkedHashMap<>(32);

        int i = 0;
        for (String res : resSet) {
            final MetricEntity metricEntity = metrics.get(i++);
            if (metricEntity != null) {
                resourceCount.put(res, metricEntity);
            }
        }

        return listResourcesSorted(resourceCount);
    }

    private String genId() {
        return NanoIdUtils.randomNanoId();
    }

    private String buildResHashKey(final String app) {
        return String.format("{%s}.metric.res.hash", app);
    }

    private String buildResLastMetricKey(final String app, final String res) {
        return String.format("{%s}.metric.res.last.value_%s", app, res);
    }

    private String buildDataHashKey(final String app, final String resource) {
        return String.format("{%s}.metric.data.hash_%s", app, resource);
    }

    private String buildTsKey(final String app, final String resource) {
        return String.format("{%s}.metric.ts.zset_%s", app, resource);
    }


    private Pair<Long, Long> getMaxLiveTime() {
        final long millis = metricsProperties.getMaxLiveTime().toMillis();
        final long endTime = System.currentTimeMillis();
        final long startTime = endTime - millis;
        return Pair.of(startTime, endTime);
    }

}
