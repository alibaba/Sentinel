package com.alibaba.csp.sentinel.dashboard.repository.metric;


import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.opentsdb.*;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author kangjiabang
 * @date
 */
@Repository("tsdbMetricsRepository")
public class OpenTsdbMetricsRepository implements MetricsRepository<MetricEntity>,InitializingBean {

    /**
     * opentsdb url
     */
    private  String opentsdb_ur_prefix = "";

    /**
     * 通过CacheBuilder构建一个缓存实例
     */
    private  Cache<String, Set<String>> cache = CacheBuilder.newBuilder()
            .maximumSize(100000) // 设置缓存的最大容量
            .expireAfterWrite(1, TimeUnit.DAYS) // 设置缓存在写入一天后失效
            .build();


    private static final String SENTINEL_METRIC = "sentinel_metric";

    private final Logger logger = LoggerFactory.getLogger(OpenTsdbMetricsRepository.class);


    private final BlockingQueue<MetricEntity> entityArrayBlockingDeque = new ArrayBlockingQueue<>(1000);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }

        //缓存资源信息
        cacheResource(metric);

        try {
            entityArrayBlockingDeque.put(metric);
        } catch (InterruptedException e) {
            logger.error("fail to put metricEntity in blocking queue.",e);
        }

        //List<OpenTsdbMetricEntity> openTsdbMetricEntityList = buildTsdbEntityList(metric);
        //saveElementInPsdbUseHttpClient(openTsdbMetricEntityList);

    }

    /**
     * 通过http命令执行save操作
     * @param openTsdbMetricEntityList
     */
    public void saveElementInPsdbUseHttpClient(List<OpenTsdbMetricEntity> openTsdbMetricEntityList) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(opentsdb_ur_prefix + "api/put?details");
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

        CloseableHttpResponse response = null;

        try {

            StringEntity entity = new StringEntity(JSON.toJSONString(openTsdbMetricEntityList), "UTF-8");
            httpPost.setEntity(entity);

            logger.debug("begin to save metric.");
            long startTime = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            logger.debug("save singe metric spent: {} ms",(System.currentTimeMillis() -startTime));

            HttpEntity responseEntity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            String resultString = EntityUtils.toString(responseEntity);

            logger.debug("save result:{}", resultString);

            if (statusCode == HttpStatus.SC_OK) {
                JSONObject result = JSON.parseObject(resultString);
                //如果有存储失败
                if (result.getInteger("failed") > 0) {
                    logger.error("some items store failed.");
                }

            } else {
                String message = getErrorMessage(resultString);

                logger.error("store metric failed in psdb,httpstatus:{}, errorMessage:{}", statusCode, message);
            }

            EntityUtils.consume(responseEntity);
        } catch (Exception e) {
            logger.error("fail to save metric in opentsdb,metric:{}", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("fail to close response", e);
            }
        }
    }

    /**
     * 缓存资源
     * @param metric
     */
    private void cacheResource(MetricEntity metric) {

        if (StringUtils.isEmpty(metric)) {
            return;
        }
        Set<String> resource = cache.getIfPresent(metric.getApp());
        //初始化时候双重判断
        if (cache.getIfPresent(metric.getApp()) == null) {
            synchronized (cache) {

                if (cache.getIfPresent(metric.getApp()) == null) {

                    cache.put(metric.getApp(),new HashSet<>());
                }
                resource = cache.getIfPresent(metric.getApp());
            }
        }


        if (!StringUtils.isEmpty(metric.getResource())) {

            resource.add(metric.getResource());
        }

        cache.put(metric.getApp(), resource);
    }

    /**
     * 构建tsdb指标列表
     *
     * @param metric 限流指标
     * @return 返回tsdb 指标列表
     */
    public List<OpenTsdbMetricEntity> buildTsdbEntityList(MetricEntity metric) {

        List<OpenTsdbMetricEntity> openTsdbMetricEntityLists = new ArrayList<>();
        for (IndexEnum indexEnum : IndexEnum.values()) {

            OpenTsdbMetricEntity entity = new OpenTsdbMetricEntity();
            entity.setMetric(buildMetricKey(metric.getApp(),metric.getResource()));
            entity.setTimestamp(metric.getTimestamp().getTime());
            TagInfo tagInfo = new TagInfo();
            tagInfo.setIndex(indexEnum.name());
            tagInfo.setResourceCode(String.valueOf(metric.getResourceCode()));
            entity.setTags(tagInfo);

            switch (indexEnum) {
                case block_qps:
                    entity.setValue(String.valueOf(metric.getBlockQps()));
                    break;
                case exception_qps:
                    entity.setValue(String.valueOf(metric.getExceptionQps()));
                    break;
                case success_qps:
                    entity.setValue(String.valueOf(metric.getSuccessQps()));
                    break;
                case pass_qps:
                    entity.setValue(String.valueOf(metric.getPassQps()));
                    break;
                case count:
                    entity.setValue(String.valueOf(metric.getCount()));
                    break;
                case rt:
                    entity.setValue(String.valueOf(metric.getRt()));
                    break;
                default:
                    break;
            }

            openTsdbMetricEntityLists.add(entity);
        }
        return openTsdbMetricEntityLists;
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
        List<MetricEntity> results = new ArrayList<>();
        List<MetricEntity> metricEntities = null;
        if (StringUtil.isBlank(app) || StringUtil.isBlank(resource)) {
            return results;
        }

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(opentsdb_ur_prefix + "/api/query");
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");
        CloseableHttpResponse response = null;

        try {

            OpenTsdbMetricQueryEntity openTsdbMetricQueryEntity = buildOpenTsdbMetricQueryEntity(app, resource, startTime, endTime);

            StringEntity entity = new StringEntity(JSON.toJSONString(openTsdbMetricQueryEntity), "UTF-8");
            httpPost.setEntity(entity);

            logger.debug("begin to query metric. entity:{}", JSON.toJSONString(openTsdbMetricQueryEntity));
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            logger.info("queryByAppAndResourceBetween from tsdb spent: {} ms",(System.currentTimeMillis() - start));

            HttpEntity responseEntity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            String resultString = EntityUtils.toString(responseEntity);

            logger.debug("query result:{}", resultString);

            if (statusCode == HttpStatus.SC_OK) {

                metricEntities = buildMetricEntityList(resultString,app,resource);
            } else {
                String message = getErrorMessage(resultString);

                logger.warn("query metric from tsdb failed,httpstatus:{},errorMessage:{}", statusCode, message);
            }

            EntityUtils.consume(responseEntity);
        } catch (Exception e) {
            logger.error("fail to query metric from opentsdb,app:{},resource:{},startTime:{},endTime:{}",
                    app, resource, startTime, endTime, e);
        } finally {
            try {
                if (response != null) {

                    response.close();
                }
            } catch (IOException e) {
                logger.error("fail to close response", e);
            }
        }

        return metricEntities;
    }

    /**
     * 构建响应结果
     *
     * @param resultString [
     *                     {
     *                     "metric": "sentinel_metric-demo_app-sayHello",
     *                     "tags": {
     *
     *                     "index": "pass_qps",
     *                     "resourceCode": "-2012993625",
     *
     *                     },
     *                     "aggregateTags": [],
     *                     "dps": {
     *                     "1564544135": 30,
     *                     "1564544399": 30,
     *                     "1564544494": 30
     *                     }
     *                     },
     *                     {
     *                     "metric": "sentinel_metric-demo_app-sayHello",
     *                     "tags": {
     *                     "app": "demo_app",
     *                     "index": "success_qps",
     *                     "resourceCode": "-2012993625",
     *                     "resource": "sayHello"
     *                     },
     *                     "aggregateTags": [],
     *                     "dps": {
     *                     "1564544135": 100,
     *                     "1564544399": 100,
     *                     "1564544494": 100
     *                     }
     *                     }
     *                     ]
     */
    private List<MetricEntity> buildMetricEntityList(String resultString, String app, String resource) {
        List<TsdbMetricQueryResponseEntity> tsdbMetricQueryResponseEntities;

        tsdbMetricQueryResponseEntities = JSON.parseArray(resultString, TsdbMetricQueryResponseEntity.class);

        List<MetricEntity> metricEntities = new ArrayList<>();

        if (!CollectionUtils.isEmpty(tsdbMetricQueryResponseEntities)) {

            //获取时序的总数
            int totalCount = tsdbMetricQueryResponseEntities.get(0).getDps().size();

            for (int i = 0; i < totalCount; i++) {
                MetricEntity metricEntity = new MetricEntity();
                metricEntity.setApp(app);
                metricEntity.setResource(resource);
                metricEntities.add(metricEntity);
            }

            tsdbMetricQueryResponseEntities.forEach(responseElement -> {


                /*
                 * 遍历 ,设置timestamp 和 相应的指标 "dps": {
                 "1564544135": 30,
                 "1564544399": 30,
                 "1564544494": 30
                 }
                 */
                Map<String, Object> dpsMap = responseElement.getDps();

                Iterator<Map.Entry<String, Object>> itertor = dpsMap.entrySet().iterator();
                int i = 0;
                while (itertor.hasNext()) {
                    Map.Entry<String, Object> entry = itertor.next();
                    //获取对应的entity，开始设置相应的指标值
                    MetricEntity metricEntity = metricEntities.get(i);
                    switch (IndexEnum.valueOf(responseElement.getTags().getIndex())) {

                        case block_qps:
                            metricEntity.setBlockQps(((Integer) entry.getValue()).longValue());
                            break;
                        case exception_qps:
                            metricEntity.setExceptionQps(((Integer) entry.getValue()).longValue());
                            break;
                        case success_qps:
                            metricEntity.setSuccessQps(((Integer) entry.getValue()).longValue());
                            break;
                        case pass_qps:
                            metricEntity.setPassQps(((Integer) entry.getValue()).longValue());
                            break;
                        case count:
                            metricEntity.setCount((Integer) entry.getValue());
                            break;
                        case rt:
                            metricEntity.setRt(((BigDecimal) entry.getValue()).doubleValue());
                            break;
                        default:
                            break;
                    }
                    metricEntity.setTimestamp(new Timestamp(Long.valueOf(entry.getKey()) * 1000));
                    metricEntity.setGmtCreate(new Timestamp(Long.valueOf(entry.getKey()) * 1000));
                    i++;
                }
            });
        }
        logger.debug("metricEntities:{} ", metricEntities);
        return metricEntities;
    }

    private OpenTsdbMetricQueryEntity buildOpenTsdbMetricQueryEntity(String app, String resource, long startTime, long endTime) {
        OpenTsdbMetricQueryEntity openTsdbMetricQueryEntity = new OpenTsdbMetricQueryEntity();
        openTsdbMetricQueryEntity.setStart(String.valueOf(startTime));
        openTsdbMetricQueryEntity.setEnd(String.valueOf(endTime));

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setAggregator("sum");
        queryInfo.setMetric(buildMetricKey(app,resource));
        TagInfo tagInfo = new TagInfo();
        tagInfo.setResourceCode("*");
        tagInfo.setIndex("*");
        queryInfo.setTags(tagInfo);
        openTsdbMetricQueryEntity.setQueries(Lists.newArrayList(queryInfo));
        return openTsdbMetricQueryEntity;
    }

    /**
     * 获取错误信息
     *
     * @param resultString opentsdb 返回结果
     * @return 错误信息
     */
    private String getErrorMessage(String resultString) {
        JSONObject result = JSON.parseObject(resultString);
        JSONObject errorDetail = result.getJSONObject("error");

        return errorDetail.getString("message");
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        Set<String> resourceSet = cache.getIfPresent(app);

        if (CollectionUtils.isEmpty(resourceSet)) {
            return null;
        }
        return Lists.newArrayList(resourceSet.toArray(new String[0]));

    }


    @Override
    public void afterPropertiesSet() {

        opentsdb_ur_prefix = System.getProperty("sentinel_opentsdb_url");

        if (StringUtils.isEmpty(opentsdb_ur_prefix)) {
            throw new RuntimeException("jvm system  parameter sentinel_opentsdb_url can not be null");
        }

        scheduledExecutorService.scheduleAtFixedRate(new SaveEntityTask(),10,10,TimeUnit.SECONDS);
    }


    private class  SaveEntityTask implements Runnable {
        @Override
        public void run() {
            final int COUNT_PER_TIME = 10;
            logger.debug("begin to execute save,queue size:{}",entityArrayBlockingDeque.size());
            int count = 0;

            //如果大于COUNT_PER_TIME条记录，开始写入opentsdb
            while (entityArrayBlockingDeque.size() > COUNT_PER_TIME) {

                List<MetricEntity> metricEntityList = Lists.newArrayList();

                //取COUNT_PER_TIME条记录
                while (count < COUNT_PER_TIME) {
                    try {
                        metricEntityList.add(entityArrayBlockingDeque.take());
                        count++;
                    } catch (InterruptedException e) {
                        logger.error("fail to take metric entity from blocking queue.", e);
                    }
                }
                count = 0;
                List<OpenTsdbMetricEntity> openTsdbMetricEntityList = Lists.newArrayList();

                logger.debug(" metricEntityList size:{}",metricEntityList.size());
                //批量构建需要插入的数据
                metricEntityList.forEach(metricEntity -> openTsdbMetricEntityList.addAll(buildTsdbEntityList(metricEntity)));

                //开始执行操作
                saveElementInPsdbUseHttpClient(openTsdbMetricEntityList);
            }
        }
    }


    private static String buildMetricKey(String app,String resource) {
        String result =  SENTINEL_METRIC + "-" + app + "-" + resource;
        return fixIllegalCharacterOfOpentsdb(result);
    }

    public static String fixIllegalCharacterOfOpentsdb(String s) {
        StringBuilder sb = new StringBuilder();

        final int n = s.length();
        for (int i = 0; i < n; i++) {
            final char c = s.charAt(i);
            if (!(('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')
                    || ('0' <= c && c <= '9') || c == '-' || c == '_' || c == '.'
                    || c == '/' || Character.isLetter(c))) {
                sb.append("_");
            } else {
                sb.append(c);
            }

        }
        return sb.toString();
    }


    public void setOpentsdb_ur_prefix(String opentsdb_ur_prefix) {
        this.opentsdb_ur_prefix = opentsdb_ur_prefix;
    }
}
