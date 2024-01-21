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
package com.alibaba.csp.sentinel.trust.util;

import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.auth.rule.JwtRule;
import com.alibaba.csp.sentinel.util.CollectionUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;

/**
 * @author lwj
 * @since 2.0.0
 */
public final class JwtUtil {

    public static final String AUTHORIZATION = "Authorization";

    private static final String BEARER_PREFIX = "Bearer" + " ";

    private JwtUtil() {
    }

    public static String getTokenFromJwtRule(Map<String, List<String>> params, Map<String, List<String>> headers,
                                             JwtRule jwtRule) {
        if (null == jwtRule) {
            return StringUtil.EMPTY;
        }
        //Firstly,get token from the header
        if (!CollectionUtil.isEmpty(headers)) {
            Map<String, String> jwtHeaders = jwtRule.getFromHeaders();
            if (!CollectionUtil.isEmpty(jwtHeaders)) {
                for (Map.Entry<String, String> entry : jwtHeaders.entrySet()) {
                    String headerName = entry.getKey();
                    String prefix = entry.getValue();
                    List<String> auths = headers.get(headerName);
                    if (!CollectionUtil.isEmpty(auths)) {
                        String token = auths.get(0);
                        if (token.startsWith(prefix)) {
                            return token.substring(prefix.length());
                        }
                    }
                }
            }
        }
        //Secondly,get token from the parma first
        if (!CollectionUtil.isEmpty(params)) {
            List<String> fromParams = jwtRule.getFromParams();
            if (!CollectionUtil.isEmpty(fromParams)) {
                for (String fromParam : fromParams) {
                    List<String> auths = params.get(fromParam);
                    if (!CollectionUtil.isEmpty(auths)) {
                        return auths.get(0);
                    }
                }
            }
        }
        //Thirdly,get token from default header
        if (!CollectionUtil.isEmpty(headers)) {
            List<String> auths = headers.get(AUTHORIZATION);
            if (!CollectionUtil.isEmpty(auths)) {
                String token = auths.get(0);
                if (token.startsWith(BEARER_PREFIX)) {
                    return token.substring(BEARER_PREFIX.length());
                }
            }
        }
        return StringUtil.EMPTY;
    }

    public static JwtClaims extractJwtClaims(String jwks, String token) {
        if (StringUtil.isBlank(jwks) || StringUtil.isBlank(token)) {
            return null;
        }
        try {
            // don't validate jwt's attribute, just validate the sign
            JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
                .setSkipAllValidators();
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(token);
            JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);
            JwksVerificationKeyResolver jwksResolver = new JwksVerificationKeyResolver(
                jsonWebKeySet.getJsonWebKeys());
            jwtConsumerBuilder.setVerificationKeyResolver(jwksResolver);
            JwtConsumer jwtConsumer = jwtConsumerBuilder.build();
            JwtContext jwtContext = jwtConsumer.process(token);
            return jwtContext.getJwtClaims();
        } catch (JoseException e) {
            RecordLog.warn("Invalid jwks = {}", jwks);
        } catch (InvalidJwtException e) {
            RecordLog.warn("Invalid jwt token {} for jwks {}", token, jwks);
        }
        return null;
    }
}
