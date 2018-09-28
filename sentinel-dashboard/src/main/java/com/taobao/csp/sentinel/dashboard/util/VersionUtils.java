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
 * @author Eric Zhao
 * @since 0.2.1
 */
public final class VersionUtils {

    public static Optional<SentinelVersion> parseVersion(String s) {
        if (StringUtil.isBlank(s)) {
            return Optional.empty();
        }
        SentinelVersion version = new SentinelVersion();
        String[] postArr = s.split("-");
        if (postArr.length > 1) {
            version.setPostfix(postArr[1]);
        }
        String[] arr = postArr[0].split("\\.");
        if (arr.length != 3) {
            return Optional.empty();
        } else {
            try {
                version.setMajorVersion(Integer.valueOf(arr[0]))
                    .setMinorVersion(Integer.valueOf(arr[1]))
                    .setFixVersion(Integer.valueOf(arr[2]));
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
        return Optional.of(version);
    }

    private VersionUtils() {}
}
