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

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * Get version of Sentinel from {@code MANIFEST.MF} file.
 * 
 * @author jason
 * @since 0.2.1
 */
public final class VersionUtil {

    public static String getVersion(String defaultVersion) {
        try {
            String version = VersionUtil.class.getPackage().getImplementationVersion();
            return StringUtil.isBlank(version) ? defaultVersion : version;
        } catch (Throwable e) {
            RecordLog.warn("Using default version, ignore exception", e);
            return defaultVersion;
        }
    }

    private VersionUtil() {}
    
    private static int parseInt(String str) {
        if (str == null || str.length() < 1) {
            return 0;
        }
        int num = 0;
        for (int i = 0; i < str.length(); i ++) {
            char ch = str.charAt(i);
            if (ch < '0' || ch > '9') {
                break;
            }
            num = num * 10 + (ch - '0');
        }
        return num;
    }

    /**
     * Convert version in string like x.y.z or x.y.z.b into number<br />
     * Each segment has one byte space(unsigned)<br />
     * eg.<br />
     * <pre>
     * 1.2.3.4 => 01 02 03 04
     * 1.2.3   => 01 02 03 00
     * 1.2     => 01 02 00 00
     * 1       => 01 00 00 00
     * </pre>
     * 
     * @return
     */
    public static int fromVersionString(String verStr) {
        if (verStr == null || verStr.length() < 1) {
            return 0;
        }
        int[] versions = new int[] {0, 0, 0, 0};
        int index = 0;
        String segment;
        int cur = 0;
        int pos;
        do {
            if (index >= versions.length) {
                // More dots than "x.y.z.b" contains
                return 0;
            }
            pos = verStr.indexOf('.', cur);
            if (pos == -1) {
                segment = verStr.substring(cur);
            } else if (cur < pos) {
                segment = verStr.substring(cur, pos);
            } else {
                // Illegal format
                return 0;
            }
            versions[index] = parseInt(segment);
            if (versions[index] < 0 || versions[index] > 255) {
                // Out of range [0, 255]
                return 0;
            }
            cur = pos + 1;
            index ++;
        } while (pos > 0);
        return ((versions[0] & 0xff) << 24)
             | ((versions[1] & 0xff) << 16)
             | ((versions[2] & 0xff) << 8)
              | (versions[3] & 0xff);
    }
}
