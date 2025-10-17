/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsExtRepository;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricVo;

/**
 * @author leyou
 */
@Controller
@RequestMapping(value = "/metric", produces = MediaType.APPLICATION_JSON_VALUE)
public class MetricController {

    private static Logger logger = LoggerFactory.getLogger(MetricController.class);

    private static final long minute1 = 1000 * 60;
    private static final long minute5 = minute1 * 5;
    private static final long hour1 = minute1 * 60;
    private static final long day1 = hour1 * 24;
    private static final long maxQueryIntervalMs = hour1;

    @Autowired
    private MetricsExtRepository<MetricEntity, Integer> metricStore;

    @ResponseBody
    @RequestMapping("/queryTopResourceMetric.json")
    public Result<?> queryTopResourceMetric(final String app,
                                            Integer pageIndex,
                                            Integer pageSize,
                                            Boolean desc,
                                            Long startTime, Long endTime,
                                            Integer cycle, String searchKey) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (pageIndex == null || pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize == null) {
            pageSize = 6;
        }
        if (pageSize >= 20) {
            pageSize = 20;
        }
        if (desc == null) {
            desc = true;
        }
        if (cycle == null || cycle <= 0) {
            cycle = 1;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if(startTime == null) {
            startTime = endTime  - ( cycle == 1 ? minute5 : day1);
        }
        if (cycle == 1 && endTime - startTime > maxQueryIntervalMs) {
            return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
        }

        List<String> resources = metricStore.listResourcesOfApp(app);
        logger.debug("queryTopResourceMetric(), resources.size()={}", resources.size());

        if (resources == null || resources.isEmpty()) {
            return Result.ofSuccess(null);
        }
        if (!desc) {
            Collections.reverse(resources);
        }
        if (StringUtil.isNotEmpty(searchKey)) {
            List<String> searched = new ArrayList<>();
            for (String resource : resources) {
                if (resource.contains(searchKey)) {
                    searched.add(resource);
                }
            }
            resources = searched;
        }
        int totalPage = (resources.size() + pageSize - 1) / pageSize;
        List<String> topResource = new ArrayList<>();
        if (pageIndex <= totalPage) {
            topResource = resources.subList((pageIndex - 1) * pageSize,
                Math.min(pageIndex * pageSize, resources.size()));
        }
        final Map<String, Iterable<MetricVo>> map = new ConcurrentHashMap<>();
        logger.debug("topResource={}", topResource);
        long time = System.currentTimeMillis();
        for (final String resource : topResource) {
            List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(
                app, resource, startTime, endTime, cycle);
            logger.debug("resource={}, entities.size()={}", resource, entities == null ? "null" : entities.size());
            List<MetricVo> vos = MetricVo.fromMetricEntities(entities, resource);
            Iterable<MetricVo> vosSorted = sortMetricVoAndDistinct(vos);
            map.put(resource, vosSorted);
        }
        logger.debug("queryTopResourceMetric() total query time={} ms", System.currentTimeMillis() - time);
        Map<String, Object> resultMap = new HashMap<>(16);
        resultMap.put("totalCount", resources.size());
        resultMap.put("totalPage", totalPage);
        resultMap.put("pageIndex", pageIndex);
        resultMap.put("pageSize", pageSize);

        Map<String, Iterable<MetricVo>> map2 = new LinkedHashMap<>();
        // order matters.
        for (String identity : topResource) {
            map2.put(identity, map.get(identity));
        }
        resultMap.put("metric", map2);
        return Result.ofSuccess(resultMap);
    }


    /**
     *
     * @param app
     * @param pageIndex
     * @param pageSize
     * @param desc
     * @param timeRange  minutes of search range, 0 means all
     * @param summaryOrderBy
     * @param startTime
     * @param endTime
     * @param searchKey
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryResourceSummary.json")
    public Result<?> queryResourceSummary(final String app,
                                            Integer pageIndex,
                                            Integer pageSize,
                                            Boolean desc,
                                            Integer timeRange,
                                            Integer summaryOrderBy,
                                            Long startTime, Long endTime,
                                            String searchKey) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (pageIndex == null || pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize == null) {
            pageSize = 6;
        }
        if (pageSize >= 20) {
            pageSize = 20;
        }
        if (desc == null) {
            desc = true;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (Objects.isNull(timeRange) || timeRange <= 0) {
            startTime = 0L;
        } else {
            startTime = endTime - (minute1 * timeRange);
        }

        int resourcesTimeLimit = timeRange == 0 ? Integer.MAX_VALUE : (int) (timeRange * minute1);
        List<String> resources = metricStore.listResourcesOfApp(app, resourcesTimeLimit);
        logger.debug("queryResourceSummary(), resources.size()={}", resources.size());

        if (resources == null || resources.isEmpty()) {
            return Result.ofSuccess(null);
        }
        if (!desc) {
            Collections.reverse(resources);
        }
        if (StringUtil.isNotEmpty(searchKey)) {
            List<String> searched = new ArrayList<>();
            for (String resource : resources) {
                if (resource.contains(searchKey)) {
                    searched.add(resource);
                }
            }
            resources = searched;
        }
        int totalPage = (resources.size() + pageSize - 1) / pageSize;
        List<String> topResource = new ArrayList<>();
        if (pageIndex <= totalPage) {
            topResource = resources.subList((pageIndex - 1) * pageSize,
                    Math.min(pageIndex * pageSize, resources.size()));
        }
        ArrayList<MetricVo> resourceArray = new ArrayList<>();
        logger.debug("topResource={}", topResource);
        long time = System.currentTimeMillis();
        for (final String resource : topResource) {
            List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(
                    app, resource, startTime, endTime, 300);
            logger.debug("resource={}, entities.size()={}", resource, entities == null ? "null" : entities.size());
            if(entities.size() == 0) {
                continue;
            }

            MetricEntity metricEntity = new MetricEntity();
            entities.forEach(entity -> metricEntity.merge(entity));
            resourceArray.add(MetricVo.fromMetricEntity(metricEntity));
        }

        Iterable<MetricVo> vosSorted = sortMetricVo(resourceArray, summaryOrderBy);
        logger.debug("queryResourceSummary() total query time={} ms", System.currentTimeMillis() - time);
        Map<String, Object> resultMap = new HashMap<>(16);
        resultMap.put("totalCount", resources.size());
        resultMap.put("totalPage", totalPage);
        resultMap.put("pageIndex", pageIndex);
        resultMap.put("pageSize", pageSize);

        resultMap.put("metric", vosSorted);
        return Result.ofSuccess(resultMap);
    }


    @ResponseBody
    @RequestMapping("/queryByAppAndResource.json")
    public Result<?> queryByAppAndResource(String app, String identity, Long startTime, Long endTime) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(identity)) {
            return Result.ofFail(-1, "identity can't be null or empty");
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - 1000 * 60;
        }
        if (endTime - startTime > maxQueryIntervalMs) {
            return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
        }
        List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(
            app, identity, startTime, endTime);
        List<MetricVo> vos = MetricVo.fromMetricEntities(entities, identity);
        return Result.ofSuccess(sortMetricVoAndDistinct(vos));
    }

    private Iterable<MetricVo> sortMetricVoAndDistinct(List<MetricVo> vos) {
        if (vos == null) {
            return null;
        }
        Map<Long, MetricVo> map = new TreeMap<>();
        for (MetricVo vo : vos) {
            MetricVo oldVo = map.get(vo.getTimestamp());
            if (oldVo == null || vo.getGmtCreate() > oldVo.getGmtCreate()) {
                map.put(vo.getTimestamp(), vo);
            }
        }
        return map.values();
    }

    /**
     *
     * @param resourceArray
     * @param summaryOrderBy    1通过qps升序， 2通过qps降序,
     *                          3拒绝QPS升序, 4拒绝QPS降序,
     *                          5异常QPS升序, 6异常QPS降序
     * @return
     */
    private Iterable<MetricVo> sortMetricVo(ArrayList<MetricVo> resourceArray, Integer summaryOrderBy) {

        if(Objects.isNull(summaryOrderBy)  ) {
            return resourceArray;
        }
        resourceArray.sort((v1, v2) -> {
            if(summaryOrderBy == 1 || summaryOrderBy == 2) {
                return (int) (v1.getPassQps() - v2.getPassQps());
            } else if(summaryOrderBy == 3 || summaryOrderBy == 4) {
                return (int) (v1.getBlockQps() - v2.getBlockQps());
            } else {
                return (int) (v1.getExceptionQps() - v2.getExceptionQps());
            }
        });

        // 偶数都为倒序
        if(summaryOrderBy % 2 == 0) {
            Collections.reverse(resourceArray);
        }
        return resourceArray;
    }

}
