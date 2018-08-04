package com.alibaba.csp.sentinel.demo.datasource.zookeeper;

import org.I0Itec.zkclient.ZkClient;

/**
 * Zookeeper config sender for demo
 *
 * @author guonanjun
 */
public class ZookeeperConfigSender {

    public static void main(String[] args) throws Exception {

        final String remoteAddress = "127.0.0.1:2181";
        final String groupId = "Sentinel-Demo";
        final String dataId = "SYSTEM-CODE-DEMO-FLOW";
        final String rule = "[\n"
                + "  {\n"
                + "    \"resource\": \"TestResource\",\n"
                + "    \"controlBehavior\": 0,\n"
                + "    \"count\": 10.0,\n"
                + "    \"grade\": 1,\n"
                + "    \"limitApp\": \"default\",\n"
                + "    \"strategy\": 0\n"
                + "  }\n"
                + "]";

        ZkClient zkClient = new ZkClient(remoteAddress, 5000);
        String path = "/" + groupId + "/" + dataId;
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path, true);
        }
        zkClient.writeData(path, rule);
    }
}
