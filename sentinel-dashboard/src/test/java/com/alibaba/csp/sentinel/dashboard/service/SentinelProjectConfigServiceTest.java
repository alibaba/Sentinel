package com.alibaba.csp.sentinel.dashboard.service;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import junit.framework.TestCase;

import java.util.zip.ZipEntry;

public class SentinelProjectConfigServiceTest extends TestCase {

    public void testResolveZipEntry() {
        ZipEntry zipEntry = SentinelProjectConfigService.resolveZipEntry("app", RuleType.SYSTEM);
        assertTrue(zipEntry.getName().startsWith("app"));
        assertTrue(zipEntry.getName().contains(RuleType.SYSTEM.name()));
        assertFalse(zipEntry.getName().contains(RuleType.SYSTEM.getName()));

        ZipEntry zipEntry1 = SentinelProjectConfigService.resolveZipEntry("app1", RuleType.PARAM_FLOW);
        assertTrue(zipEntry1.getName().contains(RuleType.PARAM_FLOW.name()));
        assertFalse(zipEntry1.getName().contains(RuleType.PARAM_FLOW.getName()));
    }

    public void testGetProjectName() {
        ZipEntry zipEntry = SentinelProjectConfigService.resolveZipEntry("app", RuleType.FLOW);
        String projectName = SentinelProjectConfigService.getProjectName(zipEntry);
        assertEquals("app", projectName);
    }

    public void testGetRuleType() {
        ZipEntry zipEntry = SentinelProjectConfigService.resolveZipEntry("app", RuleType.AUTHORITY);
        RuleType ruleType = SentinelProjectConfigService.getRuleType(zipEntry);
        assertEquals(ruleType, RuleType.AUTHORITY);
    }

}