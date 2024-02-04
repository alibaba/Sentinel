package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.opentsdb.OpenTsdbMetricEntity;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpenTsdbMetricsRepositoryTest {


    private OpenTsdbMetricsRepository openTsdbMetricsRepository = null;
    @Before
    public  void initOpenTsdb() {
        //根据公司opentsdb的url修改
        String openTsdbUrl = "https://b-opentsdb.souche-inc.com/";

        openTsdbMetricsRepository = new OpenTsdbMetricsRepository();

        openTsdbMetricsRepository.setOpentsdb_ur_prefix(openTsdbUrl);
    }
    @Test
    public void testOpenTsdbMetricsRepositoryBuildEntity() {

        MetricEntity metric = buildMetricEntity();
        List<OpenTsdbMetricEntity> openTsdbMetricEntityList =  openTsdbMetricsRepository.buildTsdbEntityList(metric);

        Assert.assertTrue(openTsdbMetricEntityList != null && openTsdbMetricEntityList.size() > 0);
        System.out.println(JSON.toJSONString(openTsdbMetricEntityList));
    }

    @Test
    public void testSaveEntityInTsdb() {

        MetricEntity metric = buildMetricEntity();

        openTsdbMetricsRepository.save(metric);
    }

    @Test
    public void testQueryResourceBetweenInTsdbNoSuchTag() {

        List<MetricEntity> metricEntityList = openTsdbMetricsRepository.queryByAppAndResourceBetween("demo_app","sayHello2", DateUtils.addHours(new Date(),-2).getTime(),new Date().getTime());
        Assert.assertTrue(CollectionUtils.isEmpty(metricEntityList));
    }

    @Test
    public void testQueryResourceBetweenInTsdbNormal() {

        MetricEntity metric = buildMetricEntity();

        openTsdbMetricsRepository.save(metric);

        List<MetricEntity> metricEntityList = openTsdbMetricsRepository.queryByAppAndResourceBetween("demo_app","sayHello", DateUtils.addHours(new Date(),-8).getTime(),new Date().getTime());
        //open it when use right openTsdbUrl in @Before
        //Assert.assertTrue(!CollectionUtils.isEmpty(metricEntityList));
    }

    @Test
    public void testlistResourcesOfApp() {

        MetricEntity metric = buildMetricEntity();
        MetricEntity metric2 = buildMetricEntity2();

        openTsdbMetricsRepository.save(metric);
        openTsdbMetricsRepository.save(metric);
        openTsdbMetricsRepository.save(metric2);

        List<String> result = openTsdbMetricsRepository.listResourcesOfApp(metric.getApp());

        Assert.assertTrue(result.contains("sayHello"));
        Assert.assertTrue(result.contains("sayGoodbye"));
        Assert.assertTrue(result.size() == 2);
        System.out.println("result list:" + result);
    }

    @Test
    public void testFixIllegalCharacterOfOpentsdb() {
        String result = OpenTsdbMetricsRepository.fixIllegalCharacterOfOpentsdb("sentinel_metric-demo_app-system_load.sayhello()");
        Assert.assertTrue("sentinel_metric-demo_app-system_load.sayhello__".equals(result));
    }

    @Test
    public void testInvokeOptsdbBatch() {

        long beginTime2 = System.currentTimeMillis();
        List<OpenTsdbMetricEntity> openTsdbMetricEntityList2 = new ArrayList<>();
        for (int i=0;i< 100;i++) {
            MetricEntity metric = buildMetricEntity();
            openTsdbMetricEntityList2.addAll(openTsdbMetricsRepository.buildTsdbEntityList(metric));
        }
        openTsdbMetricsRepository.saveElementInPsdbUseHttpClient(openTsdbMetricEntityList2);
        System.out.println("time spent2: " + (System.currentTimeMillis() - beginTime2));

    }

    private MetricEntity buildMetricEntity() {
        MetricEntity metricEntity = new MetricEntity();
        metricEntity.setApp("demo_app");
        metricEntity.setResource("sayHello");
        metricEntity.setBlockQps(10l);
        metricEntity.setExceptionQps(2l);
        metricEntity.setSuccessQps(100l);
        metricEntity.setCount(200);
        metricEntity.setRt(2000l);
        metricEntity.setPassQps(30l);
        metricEntity.setTimestamp(new Date());

        return metricEntity;

    }

    private MetricEntity buildMetricEntity2() {
        MetricEntity metricEntity = new MetricEntity();
        metricEntity.setApp("demo_app");
        metricEntity.setResource("sayGoodbye");
        metricEntity.setBlockQps(10l);
        metricEntity.setExceptionQps(2l);
        metricEntity.setSuccessQps(100l);
        metricEntity.setCount(200);
        metricEntity.setRt(2000l);
        metricEntity.setPassQps(30l);
        metricEntity.setTimestamp(new Date());

        return metricEntity;

    }
}
