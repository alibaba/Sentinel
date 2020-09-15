package com.alibaba.csp.sentinel.dashboard.datasource.entity.kie;

import lombok.Data;

@Data
public class KieServerEntity {
    private String hostname = "";
    private String ip = "";
    private Integer port = -1;
    private long lastHeartbeat;
    private long heartbeatVersion;

    private String project;
    private String app;
    private String server;
    private String environment;
    private String version;
}
