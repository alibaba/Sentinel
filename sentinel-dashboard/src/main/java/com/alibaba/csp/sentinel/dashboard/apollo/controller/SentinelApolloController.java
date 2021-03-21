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
package com.alibaba.csp.sentinel.dashboard.apollo.controller;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.apollo.service.SentinelApolloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * forbid cache consistent problem.
 * export api to clear them.
 * operation using.
 *
 * @author wxq
 */
@RestController
@RequestMapping(value = "/sentinel/apollo/")
public class SentinelApolloController {

    private static final Logger logger = LoggerFactory.getLogger(SentinelApolloController.class);

    private final SentinelApolloService sentinelApolloService;

    public SentinelApolloController(SentinelApolloService sentinelApolloService) {
        this.sentinelApolloService = sentinelApolloService;
    }

    @GetMapping("/get/registered/projects")
    @AuthAction(AuthService.PrivilegeType.READ_METRIC)
    public ResponseEntity<Set<String>> getRegisteredProjects() {
        Set<String> projectNames = this.sentinelApolloService.getRegisteredProjects();
        return ResponseEntity.ok(projectNames);
    }

    @RequestMapping("/clear/registered/projects")
    @AuthAction(AuthService.PrivilegeType.ALL)
    public ResponseEntity<Set<String>> deleteRegisteredProjects() {
        Set<String> projectNames = this.sentinelApolloService.clearRegisteredProjects();
        return ResponseEntity.ok(projectNames);
    }

    @RequestMapping("/auto/registry/projects/skip/failed")
    @AuthAction(AuthService.PrivilegeType.READ_METRIC)
    public ResponseEntity<Set<String>> autoRegistryProjectsSkipFailed() {
        Set<String> registeredProjectNames = this.sentinelApolloService.autoRegistryProjectsSkipFailed();
        return ResponseEntity.ok(registeredProjectNames);
    }

    @RequestMapping("/auto/registry/projects/skip/failed/async")
    @AuthAction(AuthService.PrivilegeType.READ_METRIC)
    public ResponseEntity<String> autoRegistryProjectsSkipFailedAsync() {
        CompletableFuture<Set<String>> registeredProjectNamesCompletableFuture = this.sentinelApolloService.autoRegistryProjectsSkipFailedAsync();
        return ResponseEntity.ok("已在后台异步操作，请通过其它操作进行查询");
    }

    @RequestMapping("/clear/cannot/read/config/projects")
    @AuthAction(AuthService.PrivilegeType.ALL)
    public ResponseEntity<Set<String>> clearCannotReadConfigProjects() {
        Set<String> projectNames = this.sentinelApolloService.clearCannotReadConfigProjects();
        return ResponseEntity.ok(projectNames);
    }

    @RequestMapping("/clear/cannot/publish/config/projects")
    @AuthAction(AuthService.PrivilegeType.ALL)
    public ResponseEntity<Set<String>> clearCannotPublishConfigProjects() {
        Set<String> projectNames = this.sentinelApolloService.clearCannotPublishConfigProjects();
        return ResponseEntity.ok(projectNames);
    }

}
