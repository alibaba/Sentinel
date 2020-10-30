package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface KieServerDiscovery {
    Set<KieServerInfo> queryKieInfos(String project, String environment);

    Set<String> queryProjects();

    List<KieServerInfo> getKieInfos();

    List<KieServerLabel> getKieLabels();

    long addMachineInfo(KieServerLabel label, MachineInfo machineInfo);

    boolean removeMachineInfo(String id, String ip, int port);

    Optional<KieServerInfo> queryKieInfo(String id);

    List<String> getServerIds();

    void removeServer(String id);

    Set<MachineInfo> getMachineInfos(String id);
}
