package com.alibaba.csp.sentinel.node.metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.*;

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

}