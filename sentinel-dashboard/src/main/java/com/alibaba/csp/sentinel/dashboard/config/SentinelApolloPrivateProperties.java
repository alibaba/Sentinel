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
package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "sentinel.apollo.private")
@Validated
public class SentinelApolloPrivateProperties {

    /**
     * which app id in apollo dashboard operates.
     * recommend that distinguish with dashboard's app id.
     */
    @NotEmpty
    private String operatedAppId;

    public String getOperatedAppId() {
        return operatedAppId;
    }

    public void setOperatedAppId(String operatedAppId) {
        this.operatedAppId = operatedAppId;
    }

}
