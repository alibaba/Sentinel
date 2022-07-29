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
package com.alibaba.csp.sentinel.dashboard.rule;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
public final class ConfigUtil {

    /**
     * 流控规则
     */
    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";

    /**
     * 网关流控规则
     */
    public static final String GW_FLOW_DATA_ID_POSTFIX = "-gw-flow-rules";

    /**
     * 降级规则
     */
    public static final String DEGRADE_DATA_ID_POSTFIX = "degrade-rules";

    /**
     * 热点规则
     */
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "param-flow-rules";

    /**
     * 系统规则
     */
    public static final String SYSTEM_DATA_ID_POSTFIX = "system-rules";

    /**
     * 授权规则
     */
    public static final String AUTHORITY_DATA_ID_POSTFIX = "authority-rules";


    private ConfigUtil() {
    }


    public static String getFlowDataId(String appName) {
        return String.format("%s%s", appName, FLOW_DATA_ID_POSTFIX);
    }


    public static String getGatewayFlowDataId(String appName) {
        return String.format("%s%s", appName, GW_FLOW_DATA_ID_POSTFIX);
    }


    public static String getDegradeDataId(String appName) {
        return String.format("%s%s", appName, DEGRADE_DATA_ID_POSTFIX);
    }


    public static String getParamFlowDataId(String appName) {
        return String.format("%s%s", appName, PARAM_FLOW_DATA_ID_POSTFIX);
    }


    public static String getSystemDataId(String appName) {
        return String.format("%s%s", appName, SYSTEM_DATA_ID_POSTFIX);
    }


    public static String getAuthorityDataId(String appName) {
        return String.format("%s%s", appName, AUTHORITY_DATA_ID_POSTFIX);
    }

}
