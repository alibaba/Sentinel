package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * wrap all ZookeeperDataSource instance and all register2Property operations
 *
 * @author zhaixiaoxiang
 * @date 2019/4/8
 */
public class ZookeeperAllDataSource {
    private Map<Class, ZookeeperDataSource> clazzDataSourceMap = Maps.newConcurrentMap();
    private String serverAddr;

    public ZookeeperAllDataSource serverAddr(final String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }

    public <W,T> ZookeeperAllDataSource register(String path, Class<W> clazz, Converter<String, T> converter) {
        if (StringUtil.isBlank(this.serverAddr)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s]", this.serverAddr));
        }
        clazzDataSourceMap.put(clazz, new ZookeeperDataSource(this.serverAddr, path, converter));
        return this;
    }

    public <W,T> ZookeeperAllDataSource register(final String groupId, final String dataId, Class<W> clazz, Converter<String, T> converter) {
        if (StringUtil.isBlank(this.serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], groupId=[%s], dataId=[%s]", this.serverAddr, groupId, dataId));
        }
        clazzDataSourceMap.put(clazz, new ZookeeperDataSource(this.serverAddr, ZookeeperDataSource.getPath(groupId, dataId), converter));
        return this;
    }

    public ZookeeperAllDataSource start() throws Exception {
        for (Map.Entry<Class, ZookeeperDataSource> entry : clazzDataSourceMap.entrySet()) {
            SentinelProperty property = entry.getValue().getProperty();
            if (entry.getKey().equals(FlowRule.class)) {
                FlowRuleManager.register2Property(property);
            } else if (entry.getKey().equals(DegradeRule.class)) {
                DegradeRuleManager.register2Property(property);
            } else if (entry.getKey().equals(SystemRule.class)) {
                SystemRuleManager.register2Property(property);
            } else if (entry.getKey().equals(AuthorityRule.class)) {
                AuthorityRuleManager.register2Property(property);
            } else if (entry.getKey().getName().equals("com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule")) {
                Class<?> clazz = Class.forName("com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager");
                Method register2Property = clazz.getDeclaredMethod("register2Property", SentinelProperty.class);
                register2Property.invoke(null, property);
            } else {
                throw new IllegalArgumentException(String.format("clazz from register() is not correct, clazz=[%s]", entry.getKey().getName()));
            }
        }
        return this;
    }

    public void close() throws Exception {
        for (ZookeeperDataSource dataSource : clazzDataSourceMap.values()) {
            dataSource.close();
        }
    }
}
