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
package com.alibaba.csp.sentinel.dashboard.util;

import com.alibaba.csp.sentinel.dashboard.rule.RuleTypeEnum;
import com.alibaba.csp.sentinel.slots.block.Rule;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Save {@link Rule} to .zip file, or read {@link Rule} from .zip file.
 *
 * @author wxq
 */
public class ConfigFileUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFileUtils.class);

    /**
     * @see <a href="https://stackoverflow.com/questions/13846000/file-separators-of-path-name-of-zipentry">File separators of Path name of ZipEntry?</a>
     */
    private static final String FORWARD_SLASH = "/";

    private static final String FILENAME_SUFFIX = ".json";

    static ZipEntry resolveZipEntry(String projectName, RuleTypeEnum ruleTypeEnum) {
        return new ZipEntry(String.join(FORWARD_SLASH, projectName, ruleTypeEnum.name() + FILENAME_SUFFIX));
    }

    /**
     * @throws IllegalArgumentException if zipEntry is a directory
     * @see #resolveZipEntry(String, RuleTypeEnum)
     */
    static String getProjectName(ZipEntry zipEntry) {
        Assert.isTrue(!zipEntry.isDirectory(), "zip entry" + zipEntry + " should not be a directory");
        File file = new File(zipEntry.getName());
        return file.getParent();
    }

    /**
     * @see #resolveZipEntry(String, RuleTypeEnum)
     */
    static RuleTypeEnum getRuleTypeEnum(ZipEntry zipEntry) {
        Assert.isTrue(!zipEntry.isDirectory(), "zip entry" + zipEntry + " should not be a directory");
        String filename = new File(zipEntry.getName()).getName();
        Assert.isTrue(filename.endsWith(FILENAME_SUFFIX), "filename '" + filename + "' should ends with " + FILENAME_SUFFIX);

        // delete suffix
        String ruleTypeEnumName = filename.substring(0, filename.length() - FILENAME_SUFFIX.length());
        return RuleTypeEnum.valueOf(ruleTypeEnumName);
    }

    private static void write2ZipOutputStream(ZipOutputStream zipOutputStream, String projectName, RuleTypeEnum ruleTypeEnum, List<? extends Rule> rules) throws IOException {
        ZipEntry zipEntry = resolveZipEntry(projectName, ruleTypeEnum);

        try {
            zipOutputStream.putNextEntry(zipEntry);
            byte[] bytes = DataSourceConverterUtils.serializeToBytes(rules);
            zipOutputStream.write(bytes);
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            String message = "config export failed. project name = " + projectName;
            logger.error(message, projectName);
            throw new IOException(message, e);
        }
    }

    private static void write2ZipOutputStream(ZipOutputStream zipOutputStream, String projectName, Map<RuleTypeEnum, List<? extends Rule>> ruleTypeEnumListMap)
            throws IOException {
        for (Map.Entry<RuleTypeEnum, List<? extends Rule>> entry : ruleTypeEnumListMap.entrySet()) {
            RuleTypeEnum ruleTypeEnum = entry.getKey();
            List<? extends Rule> rules = entry.getValue();
            if (rules.size() > 0) {
                write2ZipOutputStream(zipOutputStream, projectName, ruleTypeEnum, rules);
            }
        }
    }

    public static void writeAsZipOutputStream(OutputStream outputStream, Map<String, Map<RuleTypeEnum, List<? extends Rule>>> projectName2rules)
            throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, Map<RuleTypeEnum, List<? extends Rule>>> entry : projectName2rules.entrySet()) {
                String projectName = entry.getKey();
                Map<RuleTypeEnum, List<? extends Rule>> ruleTypeEnumListMap = entry.getValue();
                if (ruleTypeEnumListMap.size() > 0) {
                    write2ZipOutputStream(zipOutputStream, projectName, ruleTypeEnumListMap);
                }
            }
        }
    }

    public static String generateZipFilename() {
        // must contain the information of time
        final String zipFilename = "sentinel_config_export_" + DateFormatUtils.format(new Date(), "yyyy_MMdd_HH_mm_ss") + ".zip";
        return zipFilename;
    }
}
