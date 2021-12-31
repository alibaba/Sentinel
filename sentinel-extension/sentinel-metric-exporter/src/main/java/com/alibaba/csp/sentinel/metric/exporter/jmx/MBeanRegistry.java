/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.csp.sentinel.metric.exporter.jmx;

import com.alibaba.csp.sentinel.log.RecordLog;

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
 * This class provides a unified interface for registering/unregistering of Metric MBean.
 *
 * @author chenglu
 * @date 2021-07-01 20:02
 * @since 1.8.3
 */
public class MBeanRegistry {
    
    private static volatile MBeanRegistry instance = new MBeanRegistry();
    
    private Map<MetricBean, String> mapBean2Name= new ConcurrentHashMap<>(8);
    
    private Map<String, MetricBean> mapName2Bean = new ConcurrentHashMap<>(8);
    
    private MBeanServer mBeanServer;
    
    public static MBeanRegistry getInstance() {
        return instance;
    }
    
    public MBeanRegistry() {
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
    public void register(MetricBean bean, String mBeanName) throws JMException {
        assert bean != null;
        try {
            ObjectName oname = new ObjectName(mBeanName);
            mBeanServer.registerMBean(bean, oname);
            mapBean2Name.put(bean, mBeanName);
            mapName2Bean.put(mBeanName, bean);
        } catch (JMException e) {
            RecordLog.warn("[MBeanRegistry] Failed to register MBean " + mBeanName, e);
            throw e;
        }
    }
    
    /**
     * unregister the MetricBean
     * @param bean MetricBean
     */
    public void unRegister(MetricBean bean) {
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
            RecordLog.warn("[MBeanRegistry] UnRegister the MetricBean fail", e);
        }
    }
    
    /**
     * find the MBean by BeanName
     * @param mBeanName mBeanName
     * @return MetricMBean
     */
    public MetricBean findMBean(String mBeanName) {
        if (mBeanName == null) {
            return null;
        }
        return mapName2Bean.get(mBeanName);
    }
    
    /**
     * list all MBeans which is registered into MBeanRegistry
     * @return MetricBeans
     */
    public List<MetricBean> listAllMBeans() {
        return new ArrayList<>(mapName2Bean.values());
    }
}
