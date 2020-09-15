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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class KieServerManagement implements KieServerDiscovery {
    @Autowired
    @Qualifier("SimpleKieDiscovery")
    KieServerDiscovery discovery;

    @PostConstruct
    public void init() {}

    @Override
    public Set<KieServerInfo> queryKieInfos(String project) {
        return discovery.queryKieInfos(project);
    }

    @Override
    public Set<String> queryProjects() {
        return discovery.queryProjects();
    }

    @Override
    public long addServerInfo(KieServerInfo serverInfo) {
        discovery.addServerInfo(serverInfo);
        return 1;
    }

    @Override
    public boolean removeServerInfo(String project, String app, String server, String environment, String version) {
        return false;
    }
}
