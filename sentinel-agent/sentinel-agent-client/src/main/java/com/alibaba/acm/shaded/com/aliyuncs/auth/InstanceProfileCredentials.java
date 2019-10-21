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

package com.alibaba.acm.shaded.com.aliyuncs.auth;

/**
 * Created by haowei.yao on 2017/9/13.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Simple session credentials with AccessKeyID, AccessKeySecret and session token.
 */
public class InstanceProfileCredentials extends BasicSessionCredentials {

    private final long expiration;
    private final double expireFact = 0.9;
    private final long refreshIntervalInMillSeconds = 10000; // 10 sec
    private long lastFailedRefreshTime = 0;

    public InstanceProfileCredentials(String accessKeyId, String accessKeySecret,
                                      String sessionToken, String expiration, long roleSessionDurationSeconds) {
        super(accessKeyId, accessKeySecret, sessionToken, roleSessionDurationSeconds);

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        parser.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = parser.parse(expiration.replace('T', ' ').replace('Z', ' '));
            this.expiration = date.getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to get valid expiration time from ECS Metadata service.");
        }
    }

    @Override
    public boolean willSoonExpire() {
        long now = System.currentTimeMillis();
        return this.roleSessionDurationSeconds * (1 - expireFact) > (expiration - now) / 1000;
    }

    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return now >= expiration - refreshIntervalInMillSeconds;
    }

    public boolean shouldRefresh() {
        long now = System.currentTimeMillis();
        if (now - lastFailedRefreshTime > refreshIntervalInMillSeconds) {
            return true;
        } else {
            return false;
        }
    }

    public void setLastFailedRefreshTime() {
        lastFailedRefreshTime = System.currentTimeMillis();
    }
}
