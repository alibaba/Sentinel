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

/**
 * Created by haowei.yao on 2017/9/14.
 */

import com.alibaba.acm.shaded.com.aliyuncs.RpcAcsRequest;
import com.alibaba.acm.shaded.com.aliyuncs.http.ProtocolType;

/**
 * @author auto create
 * @version
 */
public class AssumeRoleRequest extends RpcAcsRequest<AssumeRoleResponse> {

    public AssumeRoleRequest() {
        super("Sts", "2015-04-01", "AssumeRole");
        setProtocol(ProtocolType.HTTPS);
    }

    private Long durationSeconds;

    private String policy;

    private String roleArn;

    private String roleSessionName;

    public Long getDurationSeconds() {
        return this.durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
        putQueryParameter("DurationSeconds", String.valueOf(durationSeconds));
    }

    public String getPolicy() {
        return this.policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
        putQueryParameter("Policy", policy);
    }

    public String getRoleArn() {
        return this.roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
        putQueryParameter("RoleArn", roleArn);
    }

    public String getRoleSessionName() {
        return this.roleSessionName;
    }

    public void setRoleSessionName(String roleSessionName) {
        this.roleSessionName = roleSessionName;
        putQueryParameter("RoleSessionName", roleSessionName);
    }

    @Override
    public Class<AssumeRoleResponse> getResponseClass() {
        return AssumeRoleResponse.class;
    }

}