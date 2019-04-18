package com.alibaba.csp.sentinel.demo.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * Zookeeper ReadableDataSource Demo
 *
 * @author guonanjun
 */
public class ZookeeperDataSourceDemo {

    public static void main(String[] args) throws Exception {
        // 使用zookeeper的场景
        loadRules();

        // 方便扩展的场景
        //loadRules2();
    }

    private static void loadRules() throws Exception {

        final String remoteAddress = "127.0.0.1:2181";
        final String pathFlow = "/Sentinel-Demo/SYSTEM-CODE-DEMO-FLOW";
        final String pathSystem = "/Sentinel-Demo/SYSTEM-CODE-DEMO-SYSTEM";
        final String pathDegrade = "/Sentinel-Demo/SYSTEM-CODE-DEMO-DEGRADE";
        final String pathAuthority = "/Sentinel-Demo/SYSTEM-CODE-DEMO-AUTHORITY";
        final String pathParam = "/Sentinel-Demo/SYSTEM-CODE-DEMO-PARAM";

        ZookeeperDataSource dataSource = new ZookeeperDataSource(remoteAddress)
                .register(pathFlow, source -> JSON.parseObject((String)source, new TypeReference<List<FlowRule>>() {}))
                .register(pathSystem, source -> JSON.parseObject((String)source, new TypeReference<List<SystemRule>>() {}))
                .register(pathDegrade, source -> JSON.parseObject((String)source, new TypeReference<List<DegradeRule>>() {}))
                .register(pathAuthority, source -> JSON.parseObject((String)source, new TypeReference<List<AuthorityRule>>() {}))
                .register(pathParam, source -> JSON.parseObject((String)source, new TypeReference<List<ParamFlowRule>>() {}));

        FlowRuleManager.register2Property(dataSource.getProperty(pathFlow));
        DegradeRuleManager.register2Property(dataSource.getProperty(pathDegrade));
        SystemRuleManager.register2Property(dataSource.getProperty(pathSystem));
        AuthorityRuleManager.register2Property(dataSource.getProperty(pathAuthority));
        ParamFlowRuleManager.register2Property(dataSource.getProperty(pathParam));

        // do something ...

        dataSource.close();
    }

    private static void loadRules2() throws Exception {

        final String remoteAddress = "127.0.0.1:2181";
        // 引入groupId和dataId的概念，是为了方便和Nacos进行切换
        final String groupId = "Sentinel-Demo";
        final String flowDataId = "SYSTEM-CODE-DEMO-FLOW";
        final String degradeDataId = "SYSTEM-CODE-DEMO-DEGRADE";
        final String systemDataId = "SYSTEM-CODE-DEMO-SYSTEM";
        final String authorityDataId = "SYSTEM-CODE-DEMO-AUTHORITY";
        final String paramDataId = "SYSTEM-CODE-DEMO-PARAM";


        // 规则会持久化到zk的/groupId/flowDataId节点
        // groupId和和flowDataId可以用/开头也可以不用
        // 建议不用以/开头，目的是为了如果从Zookeeper切换到Nacos的话，只需要改数据源类名就可以
        ZookeeperDataSource dataSource = new ZookeeperDataSource(remoteAddress)
                .register(groupId, flowDataId, source -> JSON.parseObject((String)source, new TypeReference<List<FlowRule>>() {}))
                .register(groupId, systemDataId, source -> JSON.parseObject((String)source, new TypeReference<List<SystemRule>>() {}))
                .register(groupId, degradeDataId, source -> JSON.parseObject((String)source, new TypeReference<List<DegradeRule>>() {}))
                .register(groupId, authorityDataId, source -> JSON.parseObject((String)source, new TypeReference<List<AuthorityRule>>() {}))
                .register(groupId, paramDataId, source -> JSON.parseObject((String)source, new TypeReference<List<ParamFlowRule>>() {}));

        FlowRuleManager.register2Property(dataSource.getProperty(ZookeeperDataSource.getPath(groupId, flowDataId)));
        DegradeRuleManager.register2Property(dataSource.getProperty(ZookeeperDataSource.getPath(groupId, degradeDataId)));
        SystemRuleManager.register2Property(dataSource.getProperty(ZookeeperDataSource.getPath(groupId, systemDataId)));
        AuthorityRuleManager.register2Property(dataSource.getProperty(ZookeeperDataSource.getPath(groupId, authorityDataId)));
        ParamFlowRuleManager.register2Property(dataSource.getProperty(ZookeeperDataSource.getPath(groupId, paramDataId)));

        // do something...

        dataSource.close();
    }
}
