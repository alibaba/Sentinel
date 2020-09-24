package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(value = "SimpleKieDiscovery")
public class SimpleKieDiscovery implements KieServerDiscovery {
    ConcurrentHashMap<String, Set<KieServerInfo>> serverMap = new ConcurrentHashMap<>();

    @Override
    public Set<KieServerInfo> queryKieInfos(String project, String environment) {
        return serverMap.get(project).stream().filter(x-> x.getLabel().getEnvironment().equals(environment))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> queryProjects() {
        return serverMap.keySet();
    }

    @Override
    public long addServerInfo(KieServerInfo serverInfo) {
        AssertUtil.notNull(serverInfo, "serverInfo cannot be null");
        AssertUtil.notNull(serverInfo.getLabel().getProject(), "project cannot be null");

        Set<KieServerInfo> set = serverMap.computeIfAbsent(serverInfo.getLabel().getProject(), x -> new HashSet<>());
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

    @Override
    public List<String> getServerIds() {
        return serverMap.values().stream().flatMap(Collection::stream)
                .map(KieServerInfo::getId)
                .collect(Collectors.toList());
    }

    @Override
    public KieServerInfo getServerInfo(String id) {
        return serverMap.values().stream().flatMap(Collection::stream)
                .filter(x -> x.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public void removeServer(String id) {
        serverMap.values().forEach(set -> {
           Optional<KieServerInfo> kieServerInfo = set.stream().filter(info -> info.getId().equals(id))
                   .findFirst();
           kieServerInfo.ifPresent(set::remove);
        });
    }
}
