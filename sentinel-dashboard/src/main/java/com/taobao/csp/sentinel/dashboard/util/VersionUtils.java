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
package com.taobao.csp.sentinel.dashboard.util;

import java.util.Optional;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.taobao.csp.sentinel.dashboard.datasource.entity.SentinelVersion;

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
     * @param s version string
     * @return parsed {@link SentinelVersion} if the version is valid; empty if
     * there is something wrong with the format
     */
    public static Optional<SentinelVersion> parseVersion(String s) {
        if (StringUtil.isBlank(s)) {
            return Optional.empty();
        }
        try {
            SentinelVersion version = new SentinelVersion();
            String[] postArr = s.split("-");
            if (postArr.length > 1) {
                version.setPostfix(postArr[1]);
            }
            String[] arr = postArr[0].split("\\.");
            if (arr.length == 2) {
                version.setMajorVersion(Integer.valueOf(arr[0]))
                    .setMinorVersion(Integer.valueOf(arr[1]))
                    .setFixVersion(0);
            } else if (arr.length == 3) {
                version.setMajorVersion(Integer.valueOf(arr[0]))
                    .setMinorVersion(Integer.valueOf(arr[1]))
                    .setFixVersion(Integer.valueOf(arr[2]));
            } else {
                // Wrong format, return empty.
                return Optional.empty();
            }
            return Optional.of(version);
        } catch (Exception ex) {
            // Parse fail, return empty.
            return Optional.empty();
        }
    }

    private VersionUtils() {}
}
