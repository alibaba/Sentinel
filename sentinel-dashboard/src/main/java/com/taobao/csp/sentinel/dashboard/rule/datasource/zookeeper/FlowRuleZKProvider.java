package com.taobao.csp.sentinel.dashboard.rule.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 类名称：FlowRuleZKProvider
 * 类描述：从ZK获取规则
 * 开发人：朱水平【Tank】
 * 创建时间：2018/12/20.20:44
 * 修改备注：
 *
 * @version 1.0.0
 */
@Component("flowRuleZKProvider")
public class FlowRuleZKProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {
    @Value("${zookeeper.remoteAddress}")
    private String remoteAddress;
    @Autowired
    private Converter<String, List<FlowRuleEntity>> converter;

    @Autowired
    private Converter<String, List<DegradeRuleEntity>> degradeConverter;

    @Override
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
                    new ExponentialBackoffRetry(1000, 3));
            zkClient.start();
            String path = ZookeeperConfigUtil.getPath(appName, ZookeeperConfigUtil.FLOW_RULES);
            byte[] bytes = zkClient.getData().forPath(path);
            String string = new String(bytes);
            return converter.convert(string);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
        return null;
    }

    public List<DegradeRuleEntity> getDegradeRuleEntityRules(String appName) throws Exception {
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
                    new ExponentialBackoffRetry(1000, 3));
            zkClient.start();
            String path = ZookeeperConfigUtil.getPath(appName, ZookeeperConfigUtil.DEGRADE_RULES);
            byte[] bytes = zkClient.getData().forPath(path);
            String string = new String(bytes);
            return degradeConverter.convert(string);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
        return null;
    }
}
