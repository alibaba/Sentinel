package com.alibaba.cps.sentinel.metric.exporter;

import com.alibaba.csp.sentinel.metric.exporter.jmx.MBeanRegistry;
import com.alibaba.csp.sentinel.metric.exporter.jmx.MetricBean;
import org.junit.Assert;
import org.junit.Test;

import javax.management.JMException;

/**
 * {@link com.alibaba.csp.sentinel.metric.exporter.jmx.MBeanRegistry} unit test.
 *
 * @author chenglu
 * @date 2021-07-01 23:07
 */
public class MBeanRegistryTest {
    
    @Test
    public void testMBeanRegistry() throws JMException {
        MBeanRegistry mBeanRegistry = MBeanRegistry.getInstance();
    
        MetricBean metricBean = new MetricBean();
        String mBeanName = "Sentinel:type=test,name=test";
        mBeanRegistry.register(metricBean, mBeanName);
        
        MetricBean mb1 = mBeanRegistry.findMBean(mBeanName);
        Assert.assertEquals(mb1, metricBean);
        
        mBeanRegistry.unRegister(metricBean);
    
        MetricBean mb2 = mBeanRegistry.findMBean(mBeanName);
        Assert.assertNull(mb2);
    }
}
