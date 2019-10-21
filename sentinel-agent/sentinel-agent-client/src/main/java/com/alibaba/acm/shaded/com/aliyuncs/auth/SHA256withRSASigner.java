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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by haowei.yao on 2017/9/28.
 */

public class SHA256withRSASigner extends Signer{

    private static final String ALGORITHM_NAME = "SHA256withRSA";
    public static final String ENCODING = "UTF-8";

    @Override
    public String signString(String stringToSign, String accessKeySecret) {
        try {
            Signature rsaSign = Signature.getInstance("SHA256withRSA");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] keySpec = DatatypeConverter.parseBase64Binary(accessKeySecret);
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(keySpec));
            rsaSign.initSign(privateKey);
            rsaSign.update(stringToSign.getBytes(ENCODING));
            byte[] sign = rsaSign.sign();
            String signature = new String(DatatypeConverter.printBase64Binary(sign));
            return signature;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (SignatureException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    @Override
    public String signString(String stringToSign, AlibabaCloudCredentials credentials) {
        return signString(stringToSign, credentials.getAccessKeySecret());
    }

    @Override
    public String getSignerName() {
        return ALGORITHM_NAME;
    }

    @Override
    public String getSignerVersion() {
        return "1.0";
    }

    @Override
    public String getSignerType() {
        return "PRIVATEKEY";
    }
}
