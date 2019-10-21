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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.acm.shaded.com.aliyuncs.utils.Base64Helper;

@Deprecated
public class ShaHmac1 implements ISigner {

    private final static String AGLORITHM_NAME = "HmacSHA1";

    @Override
    public String signString(String source, String accessSecret)
        throws InvalidKeyException, IllegalStateException {
        try {
            Mac mac = Mac.getInstance(AGLORITHM_NAME);
            mac.init(new SecretKeySpec(
                accessSecret.getBytes(AcsURLEncoder.URL_ENCODING), AGLORITHM_NAME));
            byte[] signData = mac.doFinal(source.getBytes(AcsURLEncoder.URL_ENCODING));
            return Base64Helper.encode(signData);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("HMAC-SHA1 not supported.");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported.");
        }

    }

    @Override
    public String getSignerName() {
        return "HMAC-SHA1";
    }

    @Override
    public String getSignerVersion() {
        return "1.0";
    }
}
