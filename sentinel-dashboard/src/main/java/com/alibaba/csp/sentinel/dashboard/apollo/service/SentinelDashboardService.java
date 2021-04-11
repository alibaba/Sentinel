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
package com.alibaba.csp.sentinel.dashboard.apollo.service;

import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author wxq
 */
@Service
public class SentinelDashboardService {

    @Autowired
    private AppManagement appManagement;

    /**
     * @return all project names in sentinel dashboard's sidebar
     */
    public Set<String> getProjectNamesInSidebar() {
        Set<AppInfo> appInfos = appManagement.getBriefApps();
        Set<String> projectNamesInSidebar = appInfos.stream().map(AppInfo::getApp).collect(Collectors.toSet());
        return Collections.unmodifiableSet(new TreeSet<>(projectNamesInSidebar));
    }

}
