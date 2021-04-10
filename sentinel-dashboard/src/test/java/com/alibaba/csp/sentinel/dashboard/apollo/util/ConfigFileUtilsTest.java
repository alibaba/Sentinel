package com.alibaba.csp.sentinel.dashboard.apollo.util;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import junit.framework.TestCase;

import java.util.zip.ZipEntry;

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