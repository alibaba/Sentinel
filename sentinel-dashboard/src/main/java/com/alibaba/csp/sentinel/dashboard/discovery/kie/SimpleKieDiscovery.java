package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(value = "SimpleKieDiscovery")
public class SimpleKieDiscovery implements KieServerDiscovery {
    ConcurrentHashMap<KieServerLabel, KieServerInfo> serverMap = new ConcurrentHashMap<>();

    @Override
    public Set<KieServerInfo> queryKieInfos(String project, String environment) {

        return serverMap.entrySet().stream().filter(entry -> {
            KieServerLabel label = entry.getKey();
            return label.getProject().equals(project) && label.getEnvironment().equals(environment);
        }).map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    @Override
    public Set<String> queryProjects() {
        return serverMap.keySet().stream().map(KieServerLabel::getProject)
                .collect(Collectors.toSet());
    }

    @Override
    public List<KieServerInfo> getKieInfos() {
        return new ArrayList<>(serverMap.values());
    }

    @Override
    public List<KieServerLabel> getKieLabels() {
        return new ArrayList<>(serverMap.keySet());
    }

    @Override
    public long addMachineInfo(KieServerLabel labelInfo, MachineInfo machineInfo) {
        AssertUtil.notNull(labelInfo, "labelInfo cannot be null");
        AssertUtil.notNull(labelInfo.getProject(), "machineInfo cannot be null");

        KieServerInfo kieServerInfo = serverMap.computeIfAbsent(labelInfo,
                o -> new KieServerInfo(UUID.randomUUID().toString(), labelInfo));

        kieServerInfo.addMachine(machineInfo);
        return 1;
    }

    @Override
    public boolean removeMachineInfo(String id, String ip, int port) {
        AssertUtil.assertNotBlank(id, "id cannot be blank");
        Optional<KieServerInfo> kieServerInfo = serverMap.values().stream()
                .filter(value -> value.getId().equals(id))
                .findFirst();
        if(!kieServerInfo.isPresent()){
            String errorMessage = String.format("Cannot find kie server by id: %s.", id);
            RecordLog.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        KieServerInfo kieServer = kieServerInfo.get();
        return kieServer.removeMachine(ip,port);
    }

    @Override
    public Optional<KieServerInfo> queryKieInfo(String id) {
       return serverMap.values().stream()
               .filter(value -> value.getId().equals(id))
               .findFirst();
    }

    @Override
    public List<String> getServerIds() {
        return serverMap.values().stream().map(KieServerInfo::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void removeServer(String id) {
        serverMap.entrySet().removeIf(entry -> entry.getValue().getId().equals(id));
    }

    @Override
    public Set<MachineInfo> getMachineInfos(String serverId) {
        Optional<KieServerInfo> kieServerInfo = queryKieInfo(serverId);
        return kieServerInfo.map(AppInfo::getMachines).orElse(null);
    }
}
