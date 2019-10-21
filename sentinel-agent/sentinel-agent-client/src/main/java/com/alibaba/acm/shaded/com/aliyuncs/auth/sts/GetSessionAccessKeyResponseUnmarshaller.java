/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.acm.shaded.com.aliyuncs.auth.sts;

import com.alibaba.acm.shaded.com.aliyuncs.auth.sts.GenerateSessionAccessKeyResponse.SessionAccessKey;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;

/**
 * Created by zhangw on 2017/8/10.
 */
public class GetSessionAccessKeyResponseUnmarshaller {

    public static GenerateSessionAccessKeyResponse unmarshall(GenerateSessionAccessKeyResponse getSessionAccessKeyResponse,
                                                              UnmarshallerContext context) {

        getSessionAccessKeyResponse.setRequestId(context.stringValue("GenerateSessionAccessKeyResponse.RequestId"));

        SessionAccessKey credentials = new SessionAccessKey();
        credentials.setSessionAccessKeyId(context.stringValue("GenerateSessionAccessKeyResponse.SessionAccessKey.SessionAccessKeyId"));
        credentials.setSessionAccessKeySecert(context.stringValue("GenerateSessionAccessKeyResponse.SessionAccessKey.SessionAccessKeySecret"));
        credentials.setExpiration(context.stringValue("GenerateSessionAccessKeyResponse.SessionAccessKey.Expiration"));

        getSessionAccessKeyResponse.setSessionAccessKey(credentials);

        return getSessionAccessKeyResponse;
    }
}
