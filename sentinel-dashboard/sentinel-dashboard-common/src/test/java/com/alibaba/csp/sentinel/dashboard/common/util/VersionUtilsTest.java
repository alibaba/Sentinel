/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.common.util;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

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
