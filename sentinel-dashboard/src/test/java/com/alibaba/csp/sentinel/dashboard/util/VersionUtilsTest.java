package com.alibaba.csp.sentinel.dashboard.util;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.SentinelVersion;

public class VersionUtilsTest {
    @Test
    public void test() {
        Optional<SentinelVersion> version = VersionUtils.parseVersion("1.2.3");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(2, version.get().getMinorVersion());
        assertEquals(3, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("1.2");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(2, version.get().getMinorVersion());
        assertEquals(0, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("1.");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(0, version.get().getMinorVersion());
        assertEquals(0, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("1.2.");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(2, version.get().getMinorVersion());
        assertEquals(0, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("1.2.3.");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(2, version.get().getMinorVersion());
        assertEquals(3, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("1.2.3.4");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(2, version.get().getMinorVersion());
        assertEquals(3, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("1");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(0, version.get().getMinorVersion());
        assertEquals(0, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("1.2.3-");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(2, version.get().getMinorVersion());
        assertEquals(3, version.get().getFixVersion());
        assertNull(version.get().getPostfix());
        
        version = VersionUtils.parseVersion("-");
        assertFalse(version.isPresent());
        
        version = VersionUtils.parseVersion("-t");
        assertFalse(version.isPresent());
        
        version = VersionUtils.parseVersion("");
        assertFalse(version.isPresent());
        
        version = VersionUtils.parseVersion(null);
        assertFalse(version.isPresent());
        
        version = VersionUtils.parseVersion("1.2.3-SNAPSHOTS");
        assertTrue(version.isPresent());
        assertEquals(1, version.get().getMajorVersion());
        assertEquals(2, version.get().getMinorVersion());
        assertEquals(3, version.get().getFixVersion());
        assertEquals("SNAPSHOTS", version.get().getPostfix());
    }
}
