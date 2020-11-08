package com.alibaba.csp.sentinel.demo.motan;

import com.alibaba.csp.sentinel.demo.motan.service.MotanDemoService;
import com.alibaba.csp.sentinel.demo.motan.service.impl.MotanDemoServiceImpl;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.config.ProtocolConfig;
import com.weibo.api.motan.config.RegistryConfig;
import com.weibo.api.motan.config.ServiceConfig;
import com.weibo.api.motan.util.MotanSwitcherUtil;

/**
 * author: zhangxn8
 */
public class SentinelMotanProviderService {

    public static void main(String[] args) {

        InitExecutor.doInit();

        ServiceConfig<MotanDemoService> motanDemoService = new ServiceConfig<MotanDemoService>();

        // 设置接口及实现类
        motanDemoService.setInterface(MotanDemoService.class);
        motanDemoService.setRef(new MotanDemoServiceImpl());

        // 配置服务的group以及版本号
        motanDemoService.setGroup("motan-demo-rpc");
        motanDemoService.setVersion("1.0");

        // 配置注册中心直连调用
        RegistryConfig registry = new RegistryConfig();

        //use local registry
        registry.setRegProtocol("local");

        // use ZooKeeper: 2181  or consul:8500 registry
//        registry.setRegProtocol("consul");
//        registry.setAddress("127.0.0.1:8500");

        // registry.setCheck("false"); //是否检查是否注册成功
        motanDemoService.setRegistry(registry);

        // 配置RPC协议
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setId("motan");
        protocol.setName("motan");
        motanDemoService.setProtocol(protocol);

        motanDemoService.setExport("motan:8002");
        motanDemoService.export();

        MotanSwitcherUtil.setSwitcherValue(MotanConstants.REGISTRY_HEARTBEAT_SWITCHER, true);

        System.out.println("server start...");
    }
}
