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

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.dashboard.apollo.service.ApolloPortalService;
import com.alibaba.csp.sentinel.dashboard.apollo.service.SentinelApolloService;
import com.alibaba.csp.sentinel.dashboard.apollo.service.SentinelProjectConfigService;
import com.alibaba.csp.sentinel.dashboard.apollo.util.ConfigFileUtils;
import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectConfigController.class);

    private final SentinelProjectConfigService sentinelProjectConfigService;

    private final ApolloPortalService apolloPortalService;

    public ProjectConfigController(
            SentinelProjectConfigService sentinelProjectConfigService,
            ApolloPortalService apolloPortalService
    ) {
        this.sentinelProjectConfigService = sentinelProjectConfigService;
        this.apolloPortalService = apolloPortalService;
    }

    /**
     * export one project's config to a zip file.
     *
     * @param projectName {@link SentinelConfig#PROJECT_NAME_PROP_KEY}
     */
    @GetMapping("/export")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public void export(
            @RequestParam String projectName,
            @RequestParam(required = false) RuleType ruleType,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        String filename = ConfigFileUtils.generateZipFilename();
        // log who download the configs
        logger.info(
                "Download configs, remote addr [{}], remote host [{}]. Filename is [{}], project name = [{}], rule type = [{}]",
                request.getRemoteAddr(),
                request.getRemoteHost(),
                filename,
                projectName,
                ruleType
        );
        // set downloaded filename
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);

        if (null == ruleType) {
            this.sentinelProjectConfigService.exportToZip(response.getOutputStream(), projectName);
        } else {
            this.sentinelProjectConfigService.exportToZip(response.getOutputStream(), projectName, ruleType);
        }
    }

    /**
     * Export multiple project's config base on user's choice.
     *
     * @param multipleProjectNamesTextarea multiple project names in a textarea
     */
    @PostMapping("/export/multiple")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public void exportMultiple(
            @RequestPart String multipleProjectNamesTextarea,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        String[] lines = multipleProjectNamesTextarea.split("\\R+");
        Set<String> projectNames = Arrays.stream(lines)
                // trim every lines
                .map(String::trim)
                // skip blank lines
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        String filename = ConfigFileUtils.generateZipFilename();
        // log who download the configs
        logger.info(
                "Download configs, remote addr [{}], remote host [{}]. Filename is [{}], {} projects = {}",
                request.getRemoteAddr(),
                request.getRemoteHost(),
                filename,
                projectNames.size(),
                projectNames
        );
        // set downloaded filename
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);

        this.sentinelProjectConfigService.exportToZip(response.getOutputStream(), projectNames);
    }

    /**
     * export all projects config to a zip file.
     */
    @GetMapping("/export/all")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public void export(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filename = ConfigFileUtils.generateZipFilename();
        // log who download the configs
        logger.info("Download configs, remote addr [{}], remote host [{}]. Filename is [{}]", request.getRemoteAddr(), request.getRemoteHost(), filename);
        // set downloaded filename
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);

        this.sentinelProjectConfigService.exportAllToZip(response.getOutputStream());
    }

    /**
     * Import project's rules from .zip file.
     * @param file config file. A .zip file.
     * @param jsessionid JSESSIONID in apollo portal's Cookie
     * @return key is project name, value is its rules after change by config file
     * @throws IOException if read file failed
     * @throws IllegalArgumentException if cannot authorize sentinel dashboard to manage project in apollo
     */
    @PostMapping("/import/all")
    @AuthAction(AuthService.PrivilegeType.ALL)
    public Result<Map<String, Map<RuleType, List<? extends Rule>>>> importAll(
            @RequestParam("file") MultipartFile file,
            @RequestPart(value = "JSESSIONID", required = false) String jsessionid
    ) throws IOException {
        // read all rules from file
        final Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules;
        try (InputStream inputStream = file.getInputStream()) {
            projectName2rules = ConfigFileUtils.readProjectName2Rules(inputStream);
        }

        // try to authorize sentinel dashboard to manage them in apollo
        if (StringUtils.hasText(jsessionid)) {
            // user want to authorize project in apollo
            Map<String, Boolean> assignResult = this.apolloPortalService.assignAppRoleToSentinelDashboard(jsessionid, projectName2rules.keySet());

            Set<String> successProjectNames = new HashSet<>();
            Set<String> failedProjectNames = new HashSet<>();
            for (Map.Entry<String, Boolean> entry : assignResult.entrySet()) {
                String projectName = entry.getKey();
                Boolean success = entry.getValue();
                if (success) {
                    successProjectNames.add(projectName);
                } else {
                    failedProjectNames.add(projectName);
                }
            }

            if (! failedProjectNames.isEmpty()) {
                logger.warn("there are {} projects authorize failed. {}", failedProjectNames.size(), failedProjectNames);
                throw new IllegalArgumentException("无法注册这些应用，请确认JSESSIONID填写是否正确，以及这些应用（项目）在Apollo上是否存在。" + JSON.toJSONString(failedProjectNames));
            }
        }

        // change their rules according rules in file
        final Map<String, Map<RuleType, List<? extends Rule>>> projectName2rulesSaved = this.sentinelProjectConfigService.importAllFrom(projectName2rules);

        return Result.ofSuccess(projectName2rulesSaved);
    }

}
