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
package com.alibaba.csp.sentinel.dashboard.apollo.util;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import junit.framework.TestCase;

import java.util.zip.ZipEntry;

/**
 * @author wxq
 */
public class ConfigFileUtilsTest extends TestCase {

    public void testResolveZipEntry() {
        ZipEntry zipEntry = ConfigFileUtils.resolveZipEntry("app", RuleType.SYSTEM);
        assertTrue(zipEntry.getName().startsWith("app"));
        assertTrue(zipEntry.getName().contains(RuleType.SYSTEM.name()));
        assertFalse(zipEntry.getName().contains(RuleType.SYSTEM.getName()));

        ZipEntry zipEntry1 = ConfigFileUtils.resolveZipEntry("app1", RuleType.PARAM_FLOW);
        assertTrue(zipEntry1.getName().contains(RuleType.PARAM_FLOW.name()));
        assertFalse(zipEntry1.getName().contains(RuleType.PARAM_FLOW.getName()));
    }

    public void testGetProjectName() {
        ZipEntry zipEntry = ConfigFileUtils.resolveZipEntry("app", RuleType.FLOW);
        String projectName = ConfigFileUtils.getProjectName(zipEntry);
        assertEquals("app", projectName);
    }

    public void testGetRuleType() {
        ZipEntry zipEntry = ConfigFileUtils.resolveZipEntry("app", RuleType.AUTHORITY);
        RuleType ruleType = ConfigFileUtils.getRuleType(zipEntry);
        assertEquals(ruleType, RuleType.AUTHORITY);
    }

}