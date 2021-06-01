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
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
public final class ApolloConfigUtil {

    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";

    private ApolloConfigUtil() {
    }

    public static String getFlowDataId(String appName) {
        return String.format("%s%s", appName, FLOW_DATA_ID_POSTFIX);
    }
}
