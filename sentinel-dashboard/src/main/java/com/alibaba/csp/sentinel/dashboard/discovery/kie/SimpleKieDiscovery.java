package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component(value = "SimpleKieDiscovery")
public class SimpleKieDiscovery implements KieServerDiscovery {
    ConcurrentHashMap<String, Set<KieServerInfo>> serverMap = new ConcurrentHashMap<>();

    @Override
    public Set<KieServerInfo> queryKieInfos(String project) {
        return serverMap.get(project);
    }

    @Override
    public Set<String> queryProjects() {
        return serverMap.keySet();
    }

    @Override
    public long addServerInfo(KieServerInfo serverInfo) {
        AssertUtil.notNull(serverInfo, "serverInfo cannot be null");
        AssertUtil.notNull(serverInfo.getProject(), "project cannot be null");

        Set<KieServerInfo> set = serverMap.computeIfAbsent(serverInfo.getProject(), x -> new HashSet<>());
        set.add(serverInfo);
        return 1;
    }

    @Override
    public boolean removeServerInfo(String project, String app, String server, String environment, String version) {
        return false;
    }
}
