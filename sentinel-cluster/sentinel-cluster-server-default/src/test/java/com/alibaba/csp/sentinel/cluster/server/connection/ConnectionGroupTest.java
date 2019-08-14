package com.alibaba.csp.sentinel.cluster.server.connection;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class ConnectionGroupTest {

    @Test
    public void testAddAndRemoveConnection() {
        String namespace = "test-conn-group";
        ConnectionGroup group = new ConnectionGroup(namespace);
        assertEquals(0, group.getConnectedCount());

        String address1 = "12.23.34.45:5566";
        String address2 = "192.168.0.22:32123";
        String address3 = "12.23.34.45:5566";
        group.addConnection(address1);
        assertEquals(1, group.getConnectedCount());
        group.addConnection(address2);
        assertEquals(2, group.getConnectedCount());
        group.addConnection(address3);
        assertEquals(2, group.getConnectedCount());

        group.removeConnection(address1);
        assertEquals(1, group.getConnectedCount());

        group.removeConnection(address3);
        assertEquals(1, group.getConnectedCount());
    }
}