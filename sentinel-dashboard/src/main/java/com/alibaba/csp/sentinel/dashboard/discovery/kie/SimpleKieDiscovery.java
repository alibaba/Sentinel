package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(value = "SimpleKieDiscovery")
public class SimpleKieDiscovery implements KieServerDiscovery {
    ConcurrentHashMap<String, Set<KieServerInfo>> serverMap = new ConcurrentHashMap<>();

    @Override
    public Set<KieServerInfo> queryKieInfos(String project, String environment) {
        return serverMap.get(project).stream().filter(x-> x.getEnvironment().equals(environment))
                .collect(Collectors.toSet());
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

    @Override
    public Optional<KieServerInfo> queryKieInfo(String id) {
       return serverMap.values().stream()
                .flatMap(kieServerInfos
                        -> kieServerInfos.stream()
                        .filter(y -> id.equals(y.getId())))
                .findAny();
    }
}
