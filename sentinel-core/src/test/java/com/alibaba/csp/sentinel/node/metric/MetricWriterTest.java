package com.alibaba.csp.sentinel.node.metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Carpenter Lee
 */
public class MetricWriterTest {
    @Test
    public void testFileNameCmp() {
        String[] arr = new String[] {
            "metrics.log.2018-03-06",
            "metrics.log.2018-03-07",
            "metrics.log.2018-03-07.51",
            "metrics.log.2018-03-07.10",
            "metrics.log.2018-03-06.100"
        };
        String[] key = new String[] {
            "metrics.log.2018-03-06",
            "metrics.log.2018-03-06.100",
            "metrics.log.2018-03-07",
            "metrics.log.2018-03-07.10",
            "metrics.log.2018-03-07.51"
        };
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(arr));
        Collections.sort(list, MetricWriter.METRIC_FILE_NAME_CMP);
        assertEquals(Arrays.asList(key), list);
    }

    @Test
    public void testFileNamePidCmp() {
        String[] arr = new String[] {
            "metrics.log.pid1234.2018-03-06",
            "metrics.log.pid1234.2018-03-07",
            "metrics.log.pid1234.2018-03-07.51",
            "metrics.log.pid1234.2018-03-07.10",
            "metrics.log.pid1234.2018-03-06.100"
        };
        String[] key = new String[] {
            "metrics.log.pid1234.2018-03-06",
            "metrics.log.pid1234.2018-03-06.100",
            "metrics.log.pid1234.2018-03-07",
            "metrics.log.pid1234.2018-03-07.10",
            "metrics.log.pid1234.2018-03-07.51"
        };
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(arr));
        Collections.sort(list, MetricWriter.METRIC_FILE_NAME_CMP);
        assertEquals(Arrays.asList(key), list);
    }

    @Test
    public void testFileNameMatches(){
        String baseFileName = "Sentinel-SDK-Demo-metrics.log";
        String fileName = "Sentinel-SDK-Demo-metrics.log.2018-03-06";
        assertTrue(MetricWriter.fileNameMatches(fileName, baseFileName));

        String baseFileName2 = "Sentinel-Admin-metrics.log.pid22568";
        String fileName2 = "Sentinel-Admin-metrics.log.pid22568.2018-12-24";
        assertTrue(MetricWriter.fileNameMatches(fileName2, baseFileName2));

        String baseFileName3 = "Sentinel-SDK-Demo-metrics.log";
        String fileName3 = "Sentinel-SDK-Demo-metrics.log.2018-03-06.11";
        assertTrue(MetricWriter.fileNameMatches(fileName3, baseFileName3));

        String baseFileName4 = "Sentinel-SDK-Demo-metrics.log";
        String fileName4 = "Sentinel-SDK-Demo-metrics.log.XXX.2018-03-06.11";
        assertFalse(MetricWriter.fileNameMatches(fileName4, baseFileName4));

        String baseFileName5 = "Sentinel-SDK-Demo-metrics.log";
        String fileName5 = "Sentinel-SDK-Demo-metrics.log.2018-03-06.11XXX";
        assertFalse(MetricWriter.fileNameMatches(fileName5, baseFileName5));
    }


}