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
package com.alibaba.csp.sentinel.trust.auth.rule;

import java.util.List;
import java.util.Map;

/**
 * For details see
 * https://istio.io/latest/docs/reference/config/security/jwt/
 * <p>
 * <p>
 * Not supported yet
 * <br> 1.jwks
 * <br> 2.outputPayloadToHeader
 * <br> 3.outputClaimToHeaders
 * <br> 4.forwardOriginalToken
 *
 * @author lwj
 * @since 2.0.0
 */
public class JwtRule {

    private final String name;

    private final Map<String, String> fromHeaders;

    private final String issuer;

    private final List<String> audiences;

    private final String jwks;

    private final List<String> fromParams;

    public JwtRule(String name, Map<String, String> fromHeaders, String issuer,
                   List<String> audiences, String jwks, List<String> fromParams) {
        this.name = name;
        this.fromHeaders = fromHeaders;
        this.issuer = issuer;
        this.audiences = audiences;
        this.jwks = jwks;
        this.fromParams = fromParams;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getFromHeaders() {
        return fromHeaders;
    }

    public String getIssuer() {
        return issuer;
    }

    public List<String> getAudiences() {
        return audiences;
    }

    public String getJwks() {
        return jwks;
    }

    public List<String> getFromParams() {
        return fromParams;
    }

    @Override
    public String toString() {
        return "JwtRule{" +
            "name='" + name + '\'' +
            ", fromHeaders=" + fromHeaders +
            ", issuer='" + issuer + '\'' +
            ", audiences=" + audiences +
            ", jwks='" + jwks + '\'' +
            ", fromParams=" + fromParams +
            '}';
    }
}
