/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.discovery.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class KieServerManagement implements KieServerDiscovery {
    @Autowired
    @Qualifier("SimpleKieDiscovery")
    KieServerDiscovery discovery;

    @PostConstruct
    public void init() {}

    @Override
    public Set<KieServerInfo> queryKieInfos(String project, String environment) {
        return discovery.queryKieInfos(project, environment);
    }

    @Override
    public Set<String> queryProjects() {
        return discovery.queryProjects();
    }

    @Override
    public List<KieServerInfo> getKieInfos() {
        return discovery.getKieInfos();
    }

    @Override
    public List<KieServerLabel> getKieLabels() {
        return discovery.getKieLabels();
    }

    @Override
    public long addMachineInfo(KieServerLabel label, MachineInfo machineInfo) {
        discovery.addMachineInfo(label, machineInfo);
        return 1;
    }

    @Override
    public boolean removeMachineInfo(String id, String ip, int port) {
        return discovery.removeMachineInfo(id, ip, port);
    }

    @Override
    public Optional<KieServerInfo> queryKieInfo(String id) {
        return discovery.queryKieInfo(id);
    }

    @Override
    public List<String> getServerIds(){
        return discovery.getServerIds();
    }

    @Override
    public void removeServer(String id){
        discovery.removeServer(id);
    }

    @Override
    public Set<MachineInfo> getMachineInfos(String id) {
        return discovery.getMachineInfos(id);
    }
}
