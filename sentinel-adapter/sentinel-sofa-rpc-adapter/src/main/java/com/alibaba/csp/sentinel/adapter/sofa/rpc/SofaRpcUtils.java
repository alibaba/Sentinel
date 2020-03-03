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
package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alipay.sofa.rpc.common.RemotingConstants;
import com.alipay.sofa.rpc.core.request.SofaRequest;

/**
 * @author cdfive
 */
public class SofaRpcUtils {

    public static String getApplicationName(SofaRequest request) {
        String appName = (String) request.getRequestProp(RemotingConstants.HEAD_APP_NAME);
        return appName == null ? "" : appName;
    }

    public static String getInterfaceResourceName(SofaRequest request) {
        return request.getInterfaceName();
    }

    public static String getMethodResourceName(SofaRequest request) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(request.getInterfaceName())
                .append("#")
                .append(request.getMethodName())
                .append("(");

        boolean isFirst = true;
        for (String methodArgSig : request.getMethodArgSigs()) {
            if (!isFirst) {
                buf.append(",");
            } else {
                isFirst = false;
            }

            buf.append(methodArgSig);
        }
        buf.append(")");
        return buf.toString();
    }

    public static Object[] getMethodArguments(SofaRequest request) {
        return request.getMethodArgs();
    }

    private SofaRpcUtils() {}
}
