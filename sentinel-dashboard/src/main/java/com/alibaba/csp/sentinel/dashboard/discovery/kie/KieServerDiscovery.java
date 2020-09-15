package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import java.util.List;
import java.util.Set;

public interface KieServerDiscovery {
    Set<KieServerInfo> queryKieInfos(String project);

    Set<String> queryProjects();

    long addServerInfo(KieServerInfo serverInfo);

    boolean removeServerInfo(String project, String app, String server, String environment, String version);
}
