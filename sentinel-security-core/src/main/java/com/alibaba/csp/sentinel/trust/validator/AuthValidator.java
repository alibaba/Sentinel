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
package com.alibaba.csp.sentinel.trust.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.auth.Rules;
import com.alibaba.csp.sentinel.trust.auth.condition.AuthCondition;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.Matcher;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthRule;
import com.alibaba.csp.sentinel.trust.auth.rule.JwtRule;
import com.alibaba.csp.sentinel.trust.util.JwtUtil;
import com.alibaba.csp.sentinel.util.CollectionUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

/**
 * A validator that verifies whether the request complies with Rules.
 *
 * @author lwj
 * @since 2.0.0
 */
public class AuthValidator {
    private AuthValidator() {

    }

    /**
     * The specific rules are:
     * (1) If there is a JWT rule matching the request, the request is matched. If the matching result is rejected, the
     * request is rejected.
     * (2) If any DENY policy matches the request, the request is denied.
     * (3) ALLOW the request if the workload does not have an Allow policy.
     * (4) ALLOW any Allow policy if it matches the request.
     *
     * @param request
     * @param rules
     * @return
     */
    public static boolean validate(UnifiedHttpRequest request, Rules rules) {
        if (null == request) {
            return false;
        }
        if (null == rules) {
            return true;
        }

        // The first step is to extract the corresponding token.
        for (JwtRule jwtRule : rules.getJwtRules().values()) {
            String token = JwtUtil.getTokenFromJwtRule(request.getParams(), request.getHeaders(), jwtRule);
            if (!StringUtil.isEmpty(token)) {
                JwtClaims jwtClaims = JwtUtil.extractJwtClaims(jwtRule.getJwks(), token);
                if (!validateJwtRule(jwtRule, jwtClaims)) {
                    return false;
                }
                request.setJwtClaims(jwtClaims);
                break;
            }
        }

        Map<String, AuthRule> denyRules = rules.getDenyAuthRules();
        for (AuthRule denyRule : denyRules.values()) {
            if (validateRule(denyRule, request)) {
                return false;
            }
        }

        Map<String, AuthRule> allowRules = rules.getAllowAuthRules();

        if (CollectionUtil.isEmpty(allowRules)) {
            return true;
        }

        for (AuthRule allowRule : allowRules.values()) {
            if (validateRule(allowRule, request)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verify that the Jwt rule passes
     *
     * @param jwtRule
     * @param jwtClaims
     * @return
     */
    public static boolean validateJwtRule(JwtRule jwtRule, JwtClaims jwtClaims) {
        if (null == jwtClaims) {
            return false;
        }
        try {
            if (!StringUtil.isBlank(jwtRule.getIssuer())
                && !jwtRule.getIssuer().equals(jwtClaims.getIssuer())) {
                return false;
            }

            //Include any one to pass
            if (!CollectionUtil.isEmpty(jwtRule.getAudiences())) {
                Set<String> acceptAud = new HashSet<>(jwtRule.getAudiences());
                List<String> audiences = jwtClaims.getAudience();
                boolean flag = false;
                for (String aud : audiences) {
                    if (acceptAud.contains(aud)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    return false;
                }

            }
            //Guarantee not expired
            if (null == jwtClaims.getExpirationTime()
                || jwtClaims.getExpirationTime().getValueInMillis() <= System.currentTimeMillis()) {
                return false;
            }
            return true;
        } catch (MalformedClaimException e) {
            RecordLog.warn("Invalid jwt jwtClaims ={}", jwtClaims);
        }
        return false;
    }

    public static boolean validateRule(AuthRule authRule, UnifiedHttpRequest request) {
        if (authRule.isLeaf()) {
            return validateLeafRule(authRule, request);
        }
        List<AuthRule> ruleChildren = authRule.getChildren();
        boolean res = authRule.getChildChainType() == AuthRule.ChildChainType.AND ? true : false;
        for (AuthRule ruleChild : ruleChildren) {
            boolean childRes = validateRule(ruleChild, request);
            if (!childRes && authRule.getChildChainType() == AuthRule.ChildChainType.AND) {
                res = false;
                break;
            }
            if (childRes && authRule.getChildChainType() == AuthRule.ChildChainType.OR) {
                res = true;
                break;
            }
        }
        // if "is not" is true, reverse the res
        return authRule.isNot() ? !res : res;
    }

    public static boolean validateLeafRule(AuthRule rule, UnifiedHttpRequest request) {
        try {
            AuthCondition condition = rule.getCondition();
            Matcher matcher = condition.getMatcher();
            String key = condition.getKey();
            if (matcher == null) {
                return false;
            }
            JwtClaims claims = request.getJwtClaims();
            switch (condition.getType()) {
                case DIRECT_REMOTE_IP:
                    return matcher.match(request.getSourceIp());
                case REMOTE_IP:
                    return matcher.match(request.getRemoteIp());
                case DESTINATION_IP:
                    return matcher.match(request.getDestIp());
                case HOSTS:
                    return matcher.match(request.getHost());
                case METHODS:
                    return matcher.match(request.getMethod());
                case URL_PATH:
                    return matcher.match(request.getPath());
                case AUTHENTICATED:
                    return matcher.match(request.getPrincipal());
                case DESTINATION_PORT:
                    return matcher.match(request.getPort());
                case HEADER:
                    Map<String, List<String>> headers = request.getHeaders();
                    if (null == headers) {
                        return false;
                    }
                    if (!headers.containsKey(key)) {
                        return false;
                    }
                    List<String> headerList = headers.get(key);
                    if (null == headerList) {
                        return false;
                    }
                    for (String header : headerList) {
                        if (matcher.match(header)) {
                            return true;
                        }
                    }
                    return false;

                case REQUESTED_SERVER_NAME:
                    return matcher.match(request.getSni());
                case JWT_PRINCIPALS:
                    if (claims == null) {
                        return false;
                    }
                    String issuer = claims.getIssuer();
                    String subject = claims.getSubject();
                    return matcher.match(issuer + "/" + subject);
                case JWT_AUDIENCES:
                    if (claims == null) {
                        return false;
                    }
                    List<String> audiences = claims.getAudience();
                    for (String audience : audiences) {
                        if (matcher.match(audience)) {
                            return true;
                        }
                    }
                    return false;
                case JWT_PRESENTERS:
                    if (claims == null) {
                        return false;
                    }
                    return matcher.match(claims.getClaimValueAsString("azp"));
                case JWT_CLAIMS:
                    if (claims == null) {
                        return false;
                    }
                    Object claimValue = claims.getClaimValue(key);
                    if (claimValue instanceof List) {
                        for (String claim : claims.getStringListClaimValue(key)) {
                            if (matcher.match(claim)) {
                                return true;
                            }
                        }
                    } else {
                        return matcher.match(claims.getStringClaimValue(key));
                    }
                    return false;
                default:
                    RecordLog.warn("Unsupported AuthType={}", condition.getType());
                    return false;
            }
        } catch (Exception e) {
            RecordLog.warn("Request {} doesn't match rule {}", request, rule);
        }
        return false;
    }

}
