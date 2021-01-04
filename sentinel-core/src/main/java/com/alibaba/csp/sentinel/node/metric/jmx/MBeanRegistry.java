package com.alibaba.csp.sentinel.node.metric.jmx;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricNode;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a unified interface for registering/unregistering of Metric MBean
 * @author chenglu
 */
public class MBeanRegistry {
    private static volatile MBeanRegistry instance = new MBeanRegistry();
    
    private Map<MetricNode, String> mapBean2Name= new ConcurrentHashMap<>(8);
    
    private Map<String, MetricNode> mapName2Bean = new ConcurrentHashMap<>(8);
    
    private MBeanServer mBeanServer;
    
    public static MBeanRegistry getInstance() {
        return instance;
    }
    
    public MBeanRegistry () {
        try {
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (Error e) {
            // Account for running within IKVM and create a new MBeanServer
            // if the PlatformMBeanServer does not exist.
            mBeanServer =  MBeanServerFactory.createMBeanServer();
        }
    }
    
    /**
     * Registers a new MBean with the platform MBean server.
     * @param bean the bean being registered
     * @param mBeanName the mBeanName
     * @throws JMException MBean can not register exception
     */
    public void register(MetricNode bean, String mBeanName) throws JMException {
        assert bean != null;
        try {
            ObjectName oname = new ObjectName(mBeanName);
            mBeanServer.registerMBean(bean, oname);
            mapBean2Name.put(bean, mBeanName);
            mapName2Bean.put(mBeanName, bean);
        } catch (JMException e) {
            RecordLog.warn("Failed to register MBean " + mBeanName, e);
            throw e;
        }
    }
    
    public void unRegister(MetricNode bean) {
        assert bean != null;
        String beanName = mapBean2Name.get(bean);
        if (beanName == null) {
            return;
        }
        try {
            ObjectName objectName = new ObjectName(beanName);
            mBeanServer.unregisterMBean(objectName);
            mapBean2Name.remove(bean);
            mapName2Bean.remove(beanName);
        } catch (JMException e) {
        
        }
    }
    
    public MetricNode findMBean(String mBeanName) {
        if (mBeanName == null) {
            return null;
        }
        return mapName2Bean.get(mBeanName);
    }
    
    public List<MetricNode> listAllMBeans() {
        return new ArrayList<>(mapName2Bean.values());
    }
}
