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
import junit.framework.TestCase;

import java.util.zip.ZipEntry;

/**
 * @author wxq
 */
public class ConfigFileUtilsTest extends TestCase {

    public void testResolveZipEntry() {
        ZipEntry zipEntry = ConfigFileUtils.resolveZipEntry("app", RuleTypeEnum.SYSTEM_RULE);
        assertTrue(zipEntry.getName().startsWith("app"));
        assertTrue(zipEntry.getName().contains(RuleTypeEnum.SYSTEM_RULE.name()));

        ZipEntry zipEntry1 = ConfigFileUtils.resolveZipEntry("app1", RuleTypeEnum.PARAM_FLOW_RULE);
        assertTrue(zipEntry1.getName().contains(RuleTypeEnum.PARAM_FLOW_RULE.name()));
    }

    public void testGetProjectName() {
        ZipEntry zipEntry = ConfigFileUtils.resolveZipEntry("app", RuleTypeEnum.FLOW_RULE);
        String projectName = ConfigFileUtils.getProjectName(zipEntry);
        assertEquals("app", projectName);
    }

    public void testGetRuleTypeEnum() {
        ZipEntry zipEntry = ConfigFileUtils.resolveZipEntry("app", RuleTypeEnum.AUTHORITY_RULE);
        RuleTypeEnum ruleType = ConfigFileUtils.getRuleTypeEnum(zipEntry);
        assertEquals(ruleType, RuleTypeEnum.AUTHORITY_RULE);
    }

}