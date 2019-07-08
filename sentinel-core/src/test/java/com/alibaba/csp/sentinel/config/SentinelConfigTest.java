package com.alibaba.csp.sentinel.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.alibaba.csp.sentinel.config.SentinelConfig.*;
import static com.alibaba.csp.sentinel.util.ConfigUtil.addSeparator;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link SentinelConfig}.
 *
 * @author cdfive
 */
public class SentinelConfigTest {

    @Test
    public void testDefaultConfig() {
        assertEquals(SentinelConfig.DEFAULT_CHARSET, SentinelConfig.charset());
        assertEquals(SentinelConfig.DEFAULT_SINGLE_METRIC_FILE_SIZE, SentinelConfig.singleMetricFileSize());
        assertEquals(SentinelConfig.DEFAULT_TOTAL_METRIC_FILE_COUNT, SentinelConfig.totalMetricFileCount());
        assertEquals(SentinelConfig.DEFAULT_COLD_FACTOR, SentinelConfig.coldFactor());
        assertEquals(SentinelConfig.DEFAULT_STATISTIC_MAX_RT, SentinelConfig.statisticMaxRt());
    }

    //    add JVM parameter
//    -Dcsp.sentinel.charset=gbk
//    -Dcsp.sentinel.metric.file.single.size=104857600
//    -Dcsp.sentinel.metric.file.total.count=10
//    -Dcsp.sentinel.flow.cold.factor=5
//    -Dcsp.sentinel.statistic.max.rt=10000
//    @Test
    public void testCustomConfig() {
        assertEquals("gbk", SentinelConfig.charset());
        assertEquals(104857600L, SentinelConfig.singleMetricFileSize());
        assertEquals(10, SentinelConfig.totalMetricFileCount());
        assertEquals(5, SentinelConfig.coldFactor());
        assertEquals(10000, SentinelConfig.statisticMaxRt());
    }


    /**
     * when set code factor alue equal or smaller than 1, get value
     * in SentinelConfig.coldFactor() will return DEFAULT_COLD_FACTOR
     * see {@link SentinelConfig#coldFactor()}
     */
    @Test
    public void testColdFactorEqualOrSmallerThanOne() {
        SentinelConfig.setConfig(SentinelConfig.COLD_FACTOR, "0.5");
        assertEquals(SentinelConfig.DEFAULT_COLD_FACTOR, SentinelConfig.coldFactor());

        SentinelConfig.setConfig(SentinelConfig.COLD_FACTOR, "1");
        assertEquals(SentinelConfig.DEFAULT_COLD_FACTOR, SentinelConfig.coldFactor());
    }

    @Test
    public void testColdFactoryLargerThanOne() {
        SentinelConfig.setConfig(SentinelConfig.COLD_FACTOR, "2");
        assertEquals(2, SentinelConfig.coldFactor());

        SentinelConfig.setConfig(SentinelConfig.COLD_FACTOR, "4");
        assertEquals(4, SentinelConfig.coldFactor());
    }


    //add Jvm parameter
    //-Dcsp.sentinel.config.file=sentinel-propertiesTest.properties
    //-Dcsp.sentinel.flow.cold.factor=5
    //-Dcsp.sentinel.statistic.max.rt=1000
    //@Test
    public void testLoadProperties() throws IOException {

        File file = null;
        String fileName = "sentinel-propertiesTest.properties";
        try {
            file = new File(addSeparator(System.getProperty("user.dir")) + "target/classes/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(buildPropertyStr(CHARSET, "utf-8"));
            out.write("\n");
            out.write(buildPropertyStr(SINGLE_METRIC_FILE_SIZE, "1000"));
            out.write("\n");
            out.write(buildPropertyStr(TOTAL_METRIC_FILE_COUNT, "20"));
            out.write("\n");
            out.write(buildPropertyStr(COLD_FACTOR, "123"));
            out.write("\n");
            out.write(buildPropertyStr(STATISTIC_MAX_RT, "6000"));
            out.flush();
            out.close();

            Assert.assertTrue(SentinelConfig.getConfig(CHARSET).equals("utf-8"));
            Assert.assertTrue(SentinelConfig.getConfig(SINGLE_METRIC_FILE_SIZE).equals("1000"));
            Assert.assertTrue(SentinelConfig.getConfig(TOTAL_METRIC_FILE_COUNT).equals("20"));
            Assert.assertTrue(SentinelConfig.getConfig(COLD_FACTOR).equals("5"));
            Assert.assertTrue(SentinelConfig.getConfig(STATISTIC_MAX_RT).equals("1000"));

        } finally {
            if (file != null) {
                file.delete();
            }
        }


    }

    private String buildPropertyStr(String key, String value) {
        return key + "=" + value;
    }
}