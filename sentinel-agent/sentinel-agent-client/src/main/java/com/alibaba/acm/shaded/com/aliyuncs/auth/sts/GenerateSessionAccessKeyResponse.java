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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.alibaba.acm.shaded.com.aliyuncs.AcsResponse;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangw on 2017/8/10.
 */

@XmlRootElement(name="GenerateSessionAccessKeyResponse")
public class GenerateSessionAccessKeyResponse extends AcsResponse {

    @SerializedName("RequestId")
    private String requestId;

    @SerializedName("SessionAccessKey")
    private SessionAccessKey sessionAccessKey;

    public String getRequestId() {
        return requestId;
    }

    @XmlElement(name="RequestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @XmlElement(name="SessionAccessKey")
    public SessionAccessKey getSessionAccessKey() {
        return sessionAccessKey;
    }

    public void setSessionAccessKey(SessionAccessKey sessionAccessKey) {
        this.sessionAccessKey = sessionAccessKey;
    }

    public static class SessionAccessKey {

        @SerializedName("SessionAccessKeyId")
        private String sessionAccessKeyId;

        @SerializedName("SessionAccessKeySecret")
        private String sessionAccessKeySecret;

        @SerializedName("Expiration")
        private String expiration;

        public String getSessionAccessKeyId() {
            return sessionAccessKeyId;
        }

        @XmlElement(name="SessionAccessKeyId")
        public void setSessionAccessKeyId(String sessionAccessKeyId) {
            this.sessionAccessKeyId = sessionAccessKeyId;
        }

        public String getSessionAccessKeySecert() {
            return sessionAccessKeySecret;
        }

        @XmlElement(name="SessionAccessKeySecret")
        public void setSessionAccessKeySecert(String sessionAccessKeySecert) {
            this.sessionAccessKeySecret = sessionAccessKeySecert;
        }

        public String getExpiration() {
            return expiration;
        }

        @XmlElement(name="Expiration")
        public void setExpiration(String expiration) {
            this.expiration = expiration;
        }
    }

    @Override
    public GenerateSessionAccessKeyResponse getInstance(UnmarshallerContext context) {
        return GetSessionAccessKeyResponseUnmarshaller.unmarshall(this, context);
    }
}
