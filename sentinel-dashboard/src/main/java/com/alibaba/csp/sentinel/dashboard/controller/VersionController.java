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
package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hisenyuan
 * @since 1.7.0
 */
@RestController
public class VersionController {

    private static final String VERSION_PATTERN = "-";

    @Value("${sentinel.dashboard.version:}")
    private String sentinelDashboardVersion;

    @GetMapping("/version")
    public Result<String> apiGetVersion() {
        if (StringUtil.isNotBlank(sentinelDashboardVersion)) {
            String res = sentinelDashboardVersion;
            if (sentinelDashboardVersion.contains(VERSION_PATTERN)) {
                res = sentinelDashboardVersion.substring(0, sentinelDashboardVersion.indexOf(VERSION_PATTERN));
            }
            return Result.ofSuccess(res);
        } else {
            return Result.ofFail(1, "getVersion failed: empty version");
        }
    }
}
