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
package com.alibaba.csp.sentinel.dashboard.service;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.slots.block.Rule;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.alibaba.csp.sentinel.dashboard.util.DataSourceConverterUtils.SERIALIZER;

@Service
public class SentinelProjectConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SentinelProjectConfigService.class);

    private final SentinelApolloPublicNamespaceService sentinelApolloPublicNamespaceService;

    public SentinelProjectConfigService(SentinelApolloPublicNamespaceService sentinelApolloPublicNamespaceService) {
        this.sentinelApolloPublicNamespaceService = sentinelApolloPublicNamespaceService;
    }

    private static void write2ZipOutputStream(ZipOutputStream zipOutputStream, String projectName, RuleType ruleType, List<? extends Rule> rules) throws IOException {
        ZipEntry zipEntry = new ZipEntry(String.join(File.separator, projectName, ruleType.name() + ".json"));

        try {
            zipOutputStream.putNextEntry(zipEntry);
            String jsonContent = SERIALIZER.convert(rules);
            zipOutputStream.write(jsonContent.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            String message = "config export failed. project name = " + projectName;
            logger.error(message, projectName);
            throw new IOException(message, e);
        }
    }

    private static void write2ZipOutputStream(ZipOutputStream zipOutputStream, String projectName, Map<RuleType, List<? extends Rule>> ruleTypeListMap)
            throws IOException {
        for (Map.Entry<RuleType, List<? extends Rule>> entry : ruleTypeListMap.entrySet()) {
            RuleType ruleType = entry.getKey();
            List<? extends Rule> rules = entry.getValue();
            write2ZipOutputStream(zipOutputStream, projectName, ruleType, rules);
        }
    }

    private static void writeAsZipOutputStream(OutputStream outputStream, Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules)
            throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, Map<RuleType, List<? extends Rule>>> entry : projectName2rules.entrySet()) {
                String projectName = entry.getKey();
                Map<RuleType, List<? extends Rule>> ruleTypeListMap = entry.getValue();
                write2ZipOutputStream(zipOutputStream, projectName, ruleTypeListMap);
            }
        }
    }

    public static String generateZipFilename() {
        // must contain the information of time
        final String zipFilename = "sentinel_config_export_" + DateFormatUtils.format(new Date(), "yyyy_MMdd_HH_mm_ss") + ".zip";
        return zipFilename;
    }

    public void exportAllToZip(OutputStream outputStream) throws IOException {
        Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules = this.sentinelApolloPublicNamespaceService.getRules();
        writeAsZipOutputStream(outputStream, projectName2rules);
    }

    public void exportToZip(OutputStream outputStream, String projectName) throws IOException {
        Map<RuleType, List<? extends Rule>> ruleTypeListMap = this.sentinelApolloPublicNamespaceService.getRules(projectName);
        writeAsZipOutputStream(outputStream, Collections.singletonMap(projectName, ruleTypeListMap));
    }

}
