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

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.slots.block.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.alibaba.csp.sentinel.dashboard.apollo.util.ConfigFileUtils.writeAsZipOutputStream;

@Service
public class SentinelProjectConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SentinelProjectConfigService.class);

    private final SentinelApolloService sentinelApolloService;

    public SentinelProjectConfigService(SentinelApolloService sentinelApolloService) {
        this.sentinelApolloService = sentinelApolloService;
    }


    public void exportAllToZip(OutputStream outputStream) throws IOException {
        Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules = this.sentinelApolloService.getRules();
        writeAsZipOutputStream(outputStream, projectName2rules);
    }

    public void exportToZip(OutputStream outputStream, String projectName) throws IOException {
        Map<RuleType, List<? extends Rule>> ruleTypeListMap = this.sentinelApolloService.getRules(projectName);
        writeAsZipOutputStream(outputStream, Collections.singletonMap(projectName, ruleTypeListMap));
    }

    public void exportToZip(OutputStream outputStream, String projectName, RuleType ruleType) throws IOException {
        List<? extends Rule> rules = this.sentinelApolloService.getRules(projectName, ruleType);
        Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules = Collections.singletonMap(
                projectName,
                Collections.singletonMap(ruleType, rules)
        );
        writeAsZipOutputStream(outputStream, projectName2rules);
    }

    public void exportToZip(OutputStream outputStream, Set<String> projectNames) throws IOException {
        Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules = this.sentinelApolloService.getRules(projectNames);
        writeAsZipOutputStream(outputStream, projectName2rules);
    }

    public Map<String, Map<RuleType, List<? extends Rule>>> importAllFrom(Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules) {
        // registry projects
        logger.info("import {} projects config. project names = {}", projectName2rules.size(), projectName2rules.keySet());
        for (String projectName : projectName2rules.keySet()) {
            this.sentinelApolloService.registryProjectIfNotExists(projectName);
        }

        this.sentinelApolloService.setRules(projectName2rules);

        return projectName2rules;
    }

    /**
     * @return projects that do not exist in apollo
     */
    public Set<String> getNotExistProjectNames(Set<String> projectNames) {
        return this.sentinelApolloService.getNotExistingProjectNames(projectNames);
    }

    /**
     * Please use {@link #getNotExistProjectNames(Set)} first, then use current method.
     *
     * @return projects cannot registry to sentinel dashboard
     */
    public Set<String> getCannotRegisteredProjectNames(Set<String> projectNames) {
        Predicate<String> cannotRegistry = projectName -> {
            try {
                this.sentinelApolloService.registryProjectIfNotExists(projectName);
            } catch (RuntimeException e) {
                return true;
            }
            return false;
        };

        Set<String> cannotRegisteredProjectNames = projectNames.parallelStream()
                .filter(cannotRegistry)
                .collect(Collectors.toSet());
        return Collections.unmodifiableSet(new TreeSet<>(cannotRegisteredProjectNames));
    }
}
