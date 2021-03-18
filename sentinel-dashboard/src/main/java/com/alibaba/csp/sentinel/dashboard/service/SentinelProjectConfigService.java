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
import com.alibaba.csp.sentinel.dashboard.util.DataSourceConverterUtils;
import com.alibaba.csp.sentinel.slots.block.Rule;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.alibaba.csp.sentinel.dashboard.util.DataSourceConverterUtils.SERIALIZER;

@Service
public class SentinelProjectConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SentinelProjectConfigService.class);

    private static final String FILENAME_SUFFIX = ".json";

    private final SentinelApolloService sentinelApolloService;

    public SentinelProjectConfigService(SentinelApolloService sentinelApolloService) {
        this.sentinelApolloService = sentinelApolloService;
    }

    static ZipEntry resolveZipEntry(String projectName, RuleType ruleType) {
        return new ZipEntry(String.join(File.separator, projectName, ruleType.name() + FILENAME_SUFFIX));
    }

    /**
     * @throws IllegalArgumentException if zipEntry is a directory
     */
    static String getProjectName(ZipEntry zipEntry) {
        if (zipEntry.isDirectory()) {
            throw new IllegalArgumentException(zipEntry + " should not be a directory");
        }
        File file = new File(zipEntry.getName());
        return file.getParent();
    }

    static RuleType getRuleType(ZipEntry zipEntry) {
        String filename = new File(zipEntry.getName()).getName();
        Assert.isTrue(filename.endsWith(FILENAME_SUFFIX), "filename '" + filename + "' should ends with " + FILENAME_SUFFIX);
        String ruleTypeName = filename.substring(0, filename.length() - FILENAME_SUFFIX.length());
        return RuleType.valueOf(ruleTypeName);
    }

    private static void write2ZipOutputStream(ZipOutputStream zipOutputStream, String projectName, RuleType ruleType, List<? extends Rule> rules) throws IOException {
        ZipEntry zipEntry = resolveZipEntry(projectName, ruleType);

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
            if (rules.size() > 0) {
                write2ZipOutputStream(zipOutputStream, projectName, ruleType, rules);
            }
        }
    }

    private static void writeAsZipOutputStream(OutputStream outputStream, Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules)
            throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, Map<RuleType, List<? extends Rule>>> entry : projectName2rules.entrySet()) {
                String projectName = entry.getKey();
                Map<RuleType, List<? extends Rule>> ruleTypeListMap = entry.getValue();
                if (ruleTypeListMap.size() > 0) {
                    write2ZipOutputStream(zipOutputStream, projectName, ruleTypeListMap);
                }
            }
        }
    }

    public static String generateZipFilename() {
        // must contain the information of time
        final String zipFilename = "sentinel_config_export_" + DateFormatUtils.format(new Date(), "yyyy_MMdd_HH_mm_ss") + ".zip";
        return zipFilename;
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

    private static List<? extends Rule> readRules(InputStream inputStream, RuleType ruleType) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return DataSourceConverterUtils.deserialize(bytes, ruleType);
    }

    private static Map<String, Map<RuleType, List<? extends Rule>>> readFromZipInputStream(ZipInputStream zipInputStream) throws IOException {
        Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules = new HashMap<>();
        for (
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                null != zipEntry;
                zipEntry = zipInputStream.getNextEntry()
        ) {
            if (!zipEntry.isDirectory()) {
                String projectName = getProjectName(zipEntry);
                if (!projectName2rules.containsKey(projectName)) {
                    projectName2rules.put(projectName, new HashMap<>());
                }

                RuleType ruleType = getRuleType(zipEntry);
                // read content
                List<? extends Rule> rules = readRules(zipInputStream, ruleType);
                // add rules
                projectName2rules.get(projectName).put(ruleType, rules);
            }

            zipInputStream.closeEntry();
        }

        return projectName2rules;
    }

    public Map<String, Map<RuleType, List<? extends Rule>>> importAllFrom(InputStream inputStream) throws IOException {
        final Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules;
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            projectName2rules = readFromZipInputStream(zipInputStream);
        }

        // registry projects
        for (String projectName : projectName2rules.keySet()) {
            this.sentinelApolloService.registryProjectIfNotExists(projectName);
        }

        this.sentinelApolloService.setRules(projectName2rules);

        return projectName2rules;
    }

}
