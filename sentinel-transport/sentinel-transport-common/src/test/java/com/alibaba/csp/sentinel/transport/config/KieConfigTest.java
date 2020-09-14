package com.alibaba.csp.sentinel.transport.config;

import org.junit.Assert;
import org.junit.Test;

public class KieConfigTest {

    @Test
    public void testConfigValue(){
        KieConfig kieConfig = KieConfig.getInstance();

        Assert.assertEquals(kieConfig.getApp(), "appA");
        Assert.assertEquals(kieConfig.getProject(), "default");
        Assert.assertEquals(kieConfig.getService(), "serviceA");
        Assert.assertEquals(kieConfig.getEnvironment(), "test");
        Assert.assertEquals(kieConfig.getVersion(), "1.0.0");
    }
}