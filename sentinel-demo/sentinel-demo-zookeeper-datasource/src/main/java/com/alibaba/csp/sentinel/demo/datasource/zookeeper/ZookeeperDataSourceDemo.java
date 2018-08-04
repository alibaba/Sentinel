package com.alibaba.csp.sentinel.demo.datasource.zookeeper;

import java.util.List;

import com.alibaba.csp.sentinel.datasource.DataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * Zookeeper DataSource Demo
 *
 * @author guonanjun
 */
public class ZookeeperDataSourceDemo {

    public static void main(String[] args) {
        loadRules();
    }

    private static void loadRules() {

        final String remoteAddress = "127.0.0.1:2181";
        final String groupId = "Sentinel-Demo";
        final String flowDataId = "SYSTEM-CODE-DEMO-FLOW";
        // final String degradeDataId = "SYSTEM-CODE-DEMO-DEGRADE";
        // final String systemDataId = "SYSTEM-CODE-DEMO-SYSTEM";


        DataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<>(remoteAddress, groupId, flowDataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        // DataSource<String, List<DegradeRule>> degradeRuleDataSource = new ZookeeperDataSource<>(remoteAddress, groupId, degradeDataId,
        //         source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {}));
        // DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        //
        // DataSource<String, List<SystemRule>> systemRuleDataSource = new ZookeeperDataSource<>(remoteAddress, groupId, systemDataId,
        //         source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {}));
        // SystemRuleManager.register2Property(systemRuleDataSource.getProperty());

    }
}
