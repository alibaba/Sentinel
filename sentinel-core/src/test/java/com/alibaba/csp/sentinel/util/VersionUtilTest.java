package com.alibaba.csp.sentinel.util;

import org.junit.Assert;
import org.junit.Test;

public class VersionUtilTest {
    @Test
    public void versionTest() {
        String version = VersionUtil.getVersion("1.0");
        /**
         * manifest cannot be load before package
         */
        Assert.assertEquals("1.0", version);
    }
}
