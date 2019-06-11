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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hisenyuan
 * @date 2019-05-30 10:47:50
 */
@RestController
@RequestMapping(value = "/")
public class VersionController {
    @Value("${sentinel.dashboard.version:''}")
    private String sentinelDashboardVersion;
    private static String VERSION_PATTERN = "-";

    @RequestMapping(value = "/getVersion")
    public Result<String> getVersion() {
        if (sentinelDashboardVersion != null) {
            String res = sentinelDashboardVersion;
            if (sentinelDashboardVersion.contains(VERSION_PATTERN)) {
                res = sentinelDashboardVersion.substring(0, sentinelDashboardVersion.indexOf(VERSION_PATTERN));
            }
            return Result.ofSuccess(res);
        } else {
            return Result.ofFail(1, "getVersion failed");
        }
    }
}
