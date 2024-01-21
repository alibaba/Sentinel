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
 * @author lwj
 * @since 2.0.0
 */
public enum JwtPolicyType {
    FIRST_PARTY_JWT("first-party-jwt", "/var/run/secrets/kubernetes.io/serviceaccount/token"),
    THIRD_PARTY_JWT("third-party-jwt", "/var/run/secrets/tokens/istio-token"),
    ;

    private String key;
    private String jwtPath;

    JwtPolicyType(String key, String jwtPath) {
        this.key = key;
        this.jwtPath = jwtPath;
    }

    public static JwtPolicyType getByKey(String key) {
        for (JwtPolicyType type : JwtPolicyType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public String getJwtPath() {
        return jwtPath;
    }
}
