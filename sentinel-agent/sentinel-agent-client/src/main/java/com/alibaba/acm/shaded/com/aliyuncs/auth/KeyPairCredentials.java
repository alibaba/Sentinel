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

public class KeyPairCredentials implements AlibabaCloudCredentials {

    private String     privateKeySecret;
    private String     publicKeyId;

    public KeyPairCredentials(String publicKeyId, String privateKeySecret) {
        if (publicKeyId == null || privateKeySecret == null) {
            throw new IllegalArgumentException(
                "You must provide a valid pair of Public Key ID and Private Key Secret."
            );
        }

        this.publicKeyId = publicKeyId;
        this.privateKeySecret = privateKeySecret;
    }

    @Override
    public String getAccessKeyId() {
        return publicKeyId;
    }

    @Override
    public String getAccessKeySecret() {
        return privateKeySecret;
    }
}
