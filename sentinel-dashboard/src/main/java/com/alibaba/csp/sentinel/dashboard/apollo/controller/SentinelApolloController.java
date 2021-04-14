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

import com.alibaba.csp.sentinel.dashboard.apollo.service.SentinelApolloService;
import com.alibaba.csp.sentinel.dashboard.apollo.service.SentinelDashboardService;
import com.alibaba.csp.sentinel.dashboard.apollo.util.ProjectNameUtils;
import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    @Autowired
    private SentinelApolloService sentinelApolloService;

    @Autowired
    private SentinelDashboardService sentinelDashboardService;

    @GetMapping("/get/registered/projects")
    @AuthAction(AuthService.PrivilegeType.READ_METRIC)
    public ResponseEntity<Set<String>> getRegisteredProjects() {
        Set<String> projectNames = this.sentinelApolloService.getRegisteredProjects();
        return ResponseEntity.ok(projectNames);
    }

    @GetMapping("/get/projects/in/sidebar")
    @AuthAction(AuthService.PrivilegeType.READ_METRIC)
    public ResponseEntity<Set<String>> getProjectsInSidebar() {
        Set<String> projectNames = this.sentinelDashboardService.getProjectNamesInSidebar();
        return ResponseEntity.ok(projectNames);
    }

    @GetMapping("/get/all/apps")
    @AuthAction(AuthService.PrivilegeType.READ_METRIC)
    public ResponseEntity<Set<String>> getAllApps() {
        Set<String> appIds = this.sentinelApolloService.getAllApps();
        return ResponseEntity.ok(appIds);
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

    private static Result<Map<String, Set<String>>> toResult(Map<String, Boolean> registryResult) {
        Set<String> succeedProjectNames = new HashSet<>();
        Set<String> failedProjectNames = new HashSet<>();

        for (Map.Entry<String, Boolean> entry : registryResult.entrySet()) {
            String projectName = entry.getKey();
            Boolean success = entry.getValue();
            if (success) {
                succeedProjectNames.add(projectName);
            } else {
                failedProjectNames.add(projectName);
            }
        }

        Map<String, Set<String>> resultData = new HashMap<>();
        resultData.put("succeedProjectNames", succeedProjectNames);
        resultData.put("failedProjectNames", failedProjectNames);

        final long totalCount = registryResult.size();
        final long failedCount = failedProjectNames.size();
        final long successCount = totalCount - failedCount;

        if (failedCount <= 0) {
            // all success
            String message = String.format("所有应用（%d个）注册成功", totalCount);
            return Result.ofSuccess(resultData)
                    .setMsg(message)
                    ;
        } else {
            // exist failed
            String message = String.format("总共有%d个需要注册的应用，有%d个注册成功，%d个注册失败，请检查应用（项目）在Apollo上是否存在，以及JSESSIONID填写是否正确", totalCount, successCount, failedCount);
            return Result.<Map<String, Set<String>>>ofFail(-1, message).setData(resultData);
        }
    }

    @RequestMapping(value = "/auto/registry/projects/in/sidebar", produces = MediaType.APPLICATION_JSON_VALUE)
    @AuthAction(AuthService.PrivilegeType.ALL)
    public Result<Map<String, Set<String>>> autoRegistryProjectsInSidebar(@RequestPart("JSESSIONID") String jsessionid) {
        Map<String, Boolean> registryResult = this.sentinelApolloService.autoRegistryProjectsInSidebar(jsessionid);
        return toResult(registryResult);
    }

    @PostMapping(value = "/auto/registry/multiple/projects", produces = MediaType.APPLICATION_JSON_VALUE)
    @AuthAction(AuthService.PrivilegeType.ALL)
    public Result<Map<String, Set<String>>> autoRegistryMultipleProjects(
            @RequestPart("JSESSIONID") String jsessionid,
            @RequestPart String multipleProjectNamesTextarea
    ) {
        Set<String> projectNames = ProjectNameUtils.getEffectiveProjectNames(multipleProjectNamesTextarea);
        Map<String, Boolean> registryResult = this.sentinelApolloService.autoRegistryMultipleProjects(jsessionid, projectNames);
        return toResult(registryResult);
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