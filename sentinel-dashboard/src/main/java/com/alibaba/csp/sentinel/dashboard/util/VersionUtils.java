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

import java.util.Optional;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.SentinelVersion;

/**
 * Util class for parsing version.
 *
 * @author Eric Zhao
 * @since 0.2.1
 */
public final class VersionUtils {

    /**
     * Parse version of Sentinel from raw string.
     *
     * @param versionFull version string
     * @return parsed {@link SentinelVersion} if the version is valid; empty if
     * there is something wrong with the format
     */
    public static Optional<SentinelVersion> parseVersion(String s) {
        if (StringUtil.isBlank(s)) {
            return Optional.empty();
        }
        try {
            String versionFull = s;
            SentinelVersion version = new SentinelVersion();
            
            // postfix
            int index = versionFull.indexOf("-");
            if (index == 0) {
                // Start with "-"
                return Optional.empty();
            }
            if (index == versionFull.length() - 1) {
                // End with "-"
            } else if (index > 0) {
                version.setPostfix(versionFull.substring(index + 1));
            }
            
            if (index >= 0) {
                versionFull = versionFull.substring(0, index);
            }
            
            // x.x.x
            int segment = 0;
            int[] ver = new int[3];
            while (segment < ver.length) {
                index = versionFull.indexOf('.');
                if (index < 0) {
                    if (versionFull.length() > 0) {
                        ver[segment] = Integer.valueOf(versionFull);
                    }
                    break;
                }
                ver[segment] = Integer.valueOf(versionFull.substring(0, index));
                versionFull = versionFull.substring(index + 1);
                segment ++;
            }
            
            if (ver[0] < 1) {
                // Wrong format, return empty.
                return Optional.empty();
            } else {
                return Optional.of(version
                        .setMajorVersion(ver[0])
                        .setMinorVersion(ver[1])
                        .setFixVersion(ver[2]));
            }
        } catch (Exception ex) {
            // Parse fail, return empty.
            return Optional.empty();
        }
    }

    private VersionUtils() {}
}
