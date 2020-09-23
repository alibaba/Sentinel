package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;

import java.util.Optional;
import java.util.Set;

public interface KieServerDiscovery {
    Set<KieServerInfo> queryKieInfos(String project, String environment);

    Set<String> queryProjects();

    long addServerInfo(KieServerInfo serverInfo);

    boolean removeServerInfo(String project, String app, String server, String environment, String version);

    Optional<KieServerInfo> queryKieInfo(String id);
}
