/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource.xds.constant.type;

/**
 * Types of asymmetric cryptographic algorithms,
 * If the primary type is RSA, the subtype is prime length;
 * if the primary type is ECDSA, the subtype is ellipse type
 *
 * @author lwj
 * @since 2.0.0
 */
public enum AsymCryptoType {
    RSA_1024("RSA_1024", PrimaryTypeEnum.RSA, "1024"),
    RSA_2048("RSA_2048", PrimaryTypeEnum.RSA, "2048"),
    ECDSA_112_R1("ECDSA_112_R1", PrimaryTypeEnum.ECDSA, "secp112r1"),
    ECDSA_160_R1("ECDSA_160_R1", PrimaryTypeEnum.ECDSA, "secp160r1"),
    ECDSA_256_K1("ECDSA_256_K1", PrimaryTypeEnum.ECDSA, "secp256k1"),
    ;

    private String key;
    private PrimaryTypeEnum primaryType;
    private String subType;

    AsymCryptoType(String key, PrimaryTypeEnum primaryType, String subType) {
        this.key = key;
        this.primaryType = primaryType;
        this.subType = subType;
    }

    public static AsymCryptoType getByKey(String key) {
        for (AsymCryptoType type : AsymCryptoType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public PrimaryTypeEnum getPrimaryType() {
        return primaryType;
    }

    public String getSubType() {
        return subType;
    }

    public enum PrimaryTypeEnum {
        RSA("RSA", "RSA"),
        ECDSA("ECDSA", "EC"),
        ;
        private String key;
        private String privateKeyHeader;

        PrimaryTypeEnum(String key, String privateKeyHeader) {
            this.key = key;
            this.privateKeyHeader = privateKeyHeader;
        }

        public String getKey() {
            return key;
        }

        public String getPrivateKeyHeader() {
            return privateKeyHeader;
        }
    }
}
