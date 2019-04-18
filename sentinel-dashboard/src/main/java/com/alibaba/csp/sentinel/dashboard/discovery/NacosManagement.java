package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos Client Management
 *
 * @author longqiang
 */
public class NacosManagement {

    private static final Map<String, ConfigService> CLIENT_POOL = new ConcurrentHashMap<>(16);

    private NacosManagement() { throw new IllegalStateException("Utility class"); }

    public static ConfigService getOrCreateClient(NacosMachineInfo nacosMachineInfo) {
        return getOrCreate(nacosMachineInfo.getProperties());
    }

    private static ConfigService getOrCreate(Properties properties) {
        return CLIENT_POOL.computeIfAbsent(properties.getProperty(PropertyKeyConst.SERVER_ADDR), e -> createClient(properties));
    }

    private static ConfigService createClient(Properties properties) {
        try {
            return ConfigFactory.createConfigService(properties);
        } catch (NacosException e) {
            RecordLog.warn(String.format("[NacosManagement] can't create ConfigService by properties:%s", properties), e);
        }
        return null;
    }

}
