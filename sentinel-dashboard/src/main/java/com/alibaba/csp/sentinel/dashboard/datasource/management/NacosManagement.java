package com.alibaba.csp.sentinel.dashboard.datasource.management;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos Client Management
 *
 * @author longqiang
 */
@Component(Constants.NACOS_MANAGEMENT)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "nacos")
public class NacosManagement implements DataSourceManagement<ConfigService> {

    private Map<String, ConfigService> clientPool = new ConcurrentHashMap<>(16);

    @Override
    public ConfigService getOrCreateClient(DataSourceMachineInfo dataSourceMachineInfo) {
        return getOrCreateClient((NacosMachineInfo) dataSourceMachineInfo);
    }

    @Override
    public DataSourceMachineInfo transfer(JSONObject jsonObject) {
        return jsonObject.toJavaObject(NacosMachineInfo.class);
    }

    private ConfigService getOrCreateClient(NacosMachineInfo nacosMachineInfo) {
        return getOrCreate(nacosMachineInfo.getProperties());
    }

    private ConfigService getOrCreate(String properties) {
        return clientPool.computeIfAbsent(properties, e -> createClient(properties));
    }

    private static ConfigService createClient(String properties) {
        try {
            return ConfigFactory.createConfigService(JSON.parseObject(properties, Properties.class));
        } catch (NacosException e) {
            RecordLog.warn(String.format("[NacosManagement] can't create ConfigService by properties:%s", properties), e);
        }
        return null;
    }

}
