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
package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.rule.RuleTypeEnum;
import com.alibaba.csp.sentinel.dashboard.service.SentinelProjectConfigService;
import com.alibaba.csp.sentinel.dashboard.util.ConfigFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Import or export rule of project.
 *
 * @author wxq
 */
@RestController
@RequestMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectConfigController.class);

    @Autowired
    private SentinelProjectConfigService sentinelProjectConfigService;

    /**
     * export one project's config to a zip file.
     *
     * @param projectName {@link SentinelConfig#PROJECT_NAME_PROP_KEY}
     */
    @GetMapping("/export")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public void export(
            @RequestParam String projectName,
            @RequestParam(required = false) RuleTypeEnum ruleTypeEnum,
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
                ruleTypeEnum
        );
        // set downloaded filename
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);

        if (null == ruleTypeEnum) {
            this.sentinelProjectConfigService.exportToZip(response.getOutputStream(), projectName);
        } else {
            this.sentinelProjectConfigService.exportToZip(response.getOutputStream(), projectName, ruleTypeEnum);
        }
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

}

