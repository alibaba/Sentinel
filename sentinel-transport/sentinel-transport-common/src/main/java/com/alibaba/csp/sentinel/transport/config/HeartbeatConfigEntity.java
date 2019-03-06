package com.alibaba.csp.sentinel.transport.config;

import java.net.InetSocketAddress;

/**
 * For Heartbeat Config Entity
 *
 * @author jz0630
 */
public class HeartbeatConfigEntity {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private String schema = HTTP;
    private String host;
    private Integer port = 80;
    private String path = "";
    private InetSocketAddress address;

    public HeartbeatConfigEntity(String ipPortStr) {
        if (ipPortStr.startsWith(HTTPS)) {
            schema = HTTPS;
            ipPortStr = ipPortStr.trim().substring(8);
        } else if (ipPortStr.startsWith(HTTP)) {
            ipPortStr = ipPortStr.trim().substring(7);
        }

        String[] hostPath = ipPortStr.trim().split("/");
        if(hostPath.length > 1) {
            path = "/" + hostPath[1];
        }

        String[] ipPort = hostPath[0].trim().split(":");
        host = ipPort[0].trim();
        if (ipPort.length > 1) {
            port = Integer.parseInt(ipPort[1].trim());
        }
    }

    public String getSchema() {
        return schema;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public InetSocketAddress getInetSocketAddress() {
        if(address == null) {
            address = new InetSocketAddress(host, port);
        }
        return address;
    }

    @Override
    public String toString() {
        return "DashboardConfig{" +
                "schema='" + schema + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                '}';
    }
}
