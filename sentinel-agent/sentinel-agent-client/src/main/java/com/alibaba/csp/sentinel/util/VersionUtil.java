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
}
