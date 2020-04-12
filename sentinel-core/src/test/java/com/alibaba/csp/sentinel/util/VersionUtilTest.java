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
package com.alibaba.csp.sentinel.util;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class VersionUtilTest {

    @Test
    public void testGetDefaultVersion() {
        String defaultVersion = "1.0";
        String version = VersionUtil.getVersion(defaultVersion);
        // Manifest cannot be load before package.
        Assert.assertEquals(defaultVersion, version);
    }
    
    @Test
    public void testFromVersionString() {
        assertEquals(0x01020300, VersionUtil.fromVersionString("1.2.3"));
        assertEquals(0x01020304, VersionUtil.fromVersionString("1.2.3.4"));
        assertEquals(0x0102ff04, VersionUtil.fromVersionString("1.2.255.4"));
        assertEquals(0xffffffff, VersionUtil.fromVersionString("255.255.255.255"));
        assertEquals(0, VersionUtil.fromVersionString("1.255.256.0"));
        assertEquals(0x01020000, VersionUtil.fromVersionString("1.2."));
        assertEquals(0x01000000, VersionUtil.fromVersionString("1"));
        assertEquals(0x01020000, VersionUtil.fromVersionString("1.2"));
        assertEquals(0, VersionUtil.fromVersionString("test"));
        assertEquals(0x01020300, VersionUtil.fromVersionString("1.2.3-"));
        assertEquals(0x01020300, VersionUtil.fromVersionString("1.2.3b"));
        assertEquals(0x01023c00, VersionUtil.fromVersionString("1.2.60.sec9"));
        assertEquals(0x01023c00, VersionUtil.fromVersionString("1.2.60-internal"));
    }
}
