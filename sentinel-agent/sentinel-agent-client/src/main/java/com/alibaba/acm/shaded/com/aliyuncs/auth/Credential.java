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

import java.util.Calendar;
import java.util.Date;

@Deprecated
public class Credential {

    private final Date refreshDate;
    private Date expiredDate;
    private String accessKeyId;
    private String accessSecret;
    private String securityToken;

    public Credential() {
        this.refreshDate = new Date();
    }

    public Credential(String keyId, String secret) {
        this.accessKeyId = keyId;
        this.accessSecret = secret;
        this.refreshDate = new Date();
    }

    public Credential(String keyId, String secret, String securityToken) {
        this.accessKeyId = keyId;
        this.accessSecret = secret;
        this.securityToken = securityToken;
        this.refreshDate = new Date();
    }

    public Credential(String keyId, String secret, int expiredHours) {
        this.accessKeyId = keyId;
        this.accessSecret = secret;
        this.refreshDate = new Date();

        setExpiredDate(expiredHours);
    }

    public Credential(String keyId, String secret, String securityToken, int expiredHours) {
        this.accessKeyId = keyId;
        this.accessSecret = secret;
        this.securityToken = securityToken;
        this.refreshDate = new Date();

        setExpiredDate(expiredHours);
    }

    private void setExpiredDate(int expiredHours) {
        if (expiredHours > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.HOUR, expiredHours);
            expiredDate = cal.getTime();
        }
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public boolean isExpired() {
        if (this.expiredDate == null) {
            return false;
        }
        if (this.expiredDate.after(new Date())) {
            return false;
        }
        return true;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public Date getRefreshDate() {
        return refreshDate;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

}
