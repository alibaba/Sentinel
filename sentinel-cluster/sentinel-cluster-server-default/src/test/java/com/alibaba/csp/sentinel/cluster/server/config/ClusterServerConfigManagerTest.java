package com.alibaba.csp.sentinel.cluster.server.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class ClusterServerConfigManagerTest {

    @Test
    public void testIsValidTransportConfig() {
        ServerTransportConfig badConfig1 = new ServerTransportConfig().setPort(-1);
        ServerTransportConfig badConfig2 = new ServerTransportConfig().setPort(886622);
        ServerTransportConfig goodConfig1 = new ServerTransportConfig().setPort(23456);
        assertFalse(ClusterServerConfigManager.isValidTransportConfig(badConfig1));
        assertFalse(ClusterServerConfigManager.isValidTransportConfig(badConfig2));
        assertTrue(ClusterServerConfigManager.isValidTransportConfig(goodConfig1));
    }

    @Test
    public void testIsValidFlowConfig() {
        ServerFlowConfig badConfig1 = new ServerFlowConfig().setMaxAllowedQps(-2);
        ServerFlowConfig badConfig2 = new ServerFlowConfig().setIntervalMs(1000).setSampleCount(3);
        ServerFlowConfig badConfig3 = new ServerFlowConfig().setIntervalMs(1000).setSampleCount(0);
        assertFalse(ClusterServerConfigManager.isValidFlowConfig(badConfig1));
        assertFalse(ClusterServerConfigManager.isValidFlowConfig(badConfig2));
        assertFalse(ClusterServerConfigManager.isValidFlowConfig(badConfig3));
    }
}