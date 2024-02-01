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
package com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.datasource.xds.property.repository.AuthRepository;
import com.alibaba.csp.sentinel.datasource.xds.util.MatcherUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.auth.Rules;
import com.alibaba.csp.sentinel.trust.auth.condition.AuthCondition;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.PortMatcher;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.StringMatcher;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthRule;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthType;
import com.alibaba.csp.sentinel.trust.auth.rule.JwtRule;
import com.alibaba.csp.sentinel.util.CollectionUtil;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.rbac.v3.Permission;
import io.envoyproxy.envoy.config.rbac.v3.Policy;
import io.envoyproxy.envoy.config.rbac.v3.Principal;
import io.envoyproxy.envoy.config.rbac.v3.RBAC;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtHeader;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtProvider;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;

/**
 * Filter for authentication rules
 *
 * @author lwj
 * @since 2.0.0
 */
public class AuthLdsFilter extends AbstractLdsFilter {

    private AuthRepository authRepository;

    public AuthLdsFilter(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Parsing AuthorizationPolicy Rule
     *
     * @param httpFilters
     * @return
     */

    public static void resolveRbac(List<HttpFilter> httpFilters, Map<String, AuthRule> allowAuthRules,
                                   Map<String, AuthRule> denyAuthRules) {
        Map<RBAC.Action, RBAC> rbacMap = new HashMap<>();
        for (HttpFilter httpFilter : httpFilters) {
            if (!httpFilter.getName().equals(LDS_RBAC_FILTER)) {
                continue;
            }
            try {
                io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC rbac = httpFilter.getTypedConfig().unpack(
                    io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC.class);
                if (rbac != null) {
                    /**There are multiple duplicates, and we only choose one of them */
                    if (!rbacMap.containsKey(rbac.getRules().getAction())) {
                        rbacMap.put(rbac.getRules().getAction(), rbac.getRules());
                    }

                }
            } catch (InvalidProtocolBufferException e) {
                RecordLog.warn("[XdsDataSource] Parsing RbacRule error", e);
            }
        }

        for (Map.Entry<RBAC.Action, RBAC> rbacEntry : rbacMap.entrySet()) {
            RBAC.Action action = rbacEntry.getKey();
            RBAC rbac = rbacEntry.getValue();
            for (Map.Entry<String, Policy> entry : rbac.getPoliciesMap().entrySet()) {
                AuthRule authRule = new AuthRule(AuthRule.ChildChainType.AND);

                // principals:from and some when
                AuthRule principalOr = new AuthRule(AuthRule.ChildChainType.OR);
                List<Principal> principals = entry.getValue().getPrincipalsList();
                for (Principal principal : principals) {
                    AuthRule principalAnd = resolvePrincipal(principal);
                    if (principalAnd != null && !principalAnd.isEmpty()) {
                        principalOr.addChildren(principalAnd);
                    }
                }
                if (!principalOr.isEmpty()) {
                    authRule.addChildren(principalOr);
                }
                // permission:to and some when
                AuthRule permissionOr = new AuthRule(AuthRule.ChildChainType.OR);
                List<Permission> permissions = entry.getValue().getPermissionsList();
                for (Permission permission : permissions) {
                    AuthRule permissionAnd = resolvePermission(permission);
                    if (permissionAnd != null && !permissionAnd.isEmpty()) {
                        permissionOr.addChildren(permissionAnd);
                    }
                }
                if (!permissionOr.isEmpty()) {
                    authRule.addChildren(permissionOr);
                }
                if (authRule.isEmpty()) {
                    continue;
                }
                switch (action) {
                    case UNRECOGNIZED:
                    case ALLOW:
                        allowAuthRules.put(entry.getKey(), authRule);
                        break;
                    case DENY:
                        denyAuthRules.put(entry.getKey(), authRule);
                        break;
                    default:
                        RecordLog.warn("[XdsDataSource] Unknown rbac action, {}", rbac.getAction());
                }
            }
        }
    }

    /**
     * Parsing JWT Rule
     *
     * @param httpFilters
     * @return
     */
    public static Map<String, JwtRule> resolveJWT(List<HttpFilter> httpFilters) {
        Map<String, JwtRule> jwtRules = new HashMap<>();
        /**There are multiple duplicates, and we only choose one of them */
        JwtAuthentication jwtAuthentication = null;
        for (HttpFilter httpFilter : httpFilters) {
            if (!httpFilter.getName().equals(LDS_JWT_FILTER)) {
                continue;
            }
            try {
                jwtAuthentication = httpFilter.getTypedConfig().unpack(JwtAuthentication.class);
                if (null != jwtAuthentication) {
                    break;
                }
            } catch (InvalidProtocolBufferException e) {
                RecordLog.warn("[XdsDataSource] Parsing JwtRule error", e);
            }
        }
        if (null == jwtAuthentication) {
            return jwtRules;
        }

        Map<String, JwtProvider> jwtProviders = jwtAuthentication.getProvidersMap();
        for (Map.Entry<String, JwtProvider> entry : jwtProviders.entrySet()) {
            JwtProvider provider = entry.getValue();
            Map<String, String> fromHeaders = new HashMap<>();
            for (JwtHeader header : provider.getFromHeadersList()) {
                fromHeaders.put(header.getName(), header.getValuePrefix());
            }
            jwtRules.put(entry.getKey(),
                new JwtRule(entry.getKey(), fromHeaders, provider.getIssuer(),
                    new ArrayList<>(provider.getAudiencesList()),
                    provider.getLocalJwks().getInlineString(),
                    new ArrayList<>(provider.getFromParamsList())));
        }

        return jwtRules;
    }

    public static List<HttpFilter> resolveHttpFilter(List<Listener> listeners) {
        List<HttpFilter> httpFilters = new ArrayList<>();
        for (Listener listener : listeners) {
            if (!listener.getName().equals(LDS_VIRTUAL_INBOUND)) {
                continue;
            }
            for (FilterChain filterChain : listener.getFilterChainsList()) {
                for (Filter filter : filterChain.getFiltersList()) {
                    if (!filter.getName().equals(LDS_CONNECTION_MANAGER)) {
                        continue;
                    }
                    HttpConnectionManager httpConnectionManager = unpackHttpConnectionManager(filter.getTypedConfig());
                    if (httpConnectionManager == null) {
                        continue;
                    }
                    for (HttpFilter httpFilter : httpConnectionManager.getHttpFiltersList()) {
                        if (httpFilter != null) {
                            httpFilters.add(httpFilter);
                        }
                    }
                }
            }
        }
        return httpFilters;
    }

    public static HttpConnectionManager unpackHttpConnectionManager(Any any) {
        try {
            if (!any.is(HttpConnectionManager.class)) {
                return null;
            }
            return any.unpack(HttpConnectionManager.class);
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    /**
     * Parse principal:from and some when
     *
     * @param principal
     * @return
     */
    private static AuthRule resolvePrincipal(Principal principal) {
        Principal.Set andIds = principal.getAndIds();
        AuthRule andChildren = new AuthRule(AuthRule.ChildChainType.AND);
        for (Principal andId : andIds.getIdsList()) {
            if (andId.getAny()) {
                return null;
            }
            //If not, go further
            boolean isNot = false;
            if (andId.hasNotId()) {
                isNot = true;
                andId = andId.getNotId();
            }
            AuthRule orChildren = new AuthRule(AuthRule.ChildChainType.OR, isNot);
            Principal.Set orIds = andId.getOrIds();
            for (Principal orId : orIds.getIdsList()) {
                Principal.IdentifierCase identifierCase = orId.getIdentifierCase();

                if (Principal.IdentifierCase.AUTHENTICATED == identifierCase) {
                    if (null == orId.getAuthenticated() || null == orId.getAuthenticated().getPrincipalName()) {
                        continue;
                    }
                    StringMatcher stringMatcher = MatcherUtil.convStringMatcher(
                        orId.getAuthenticated().getPrincipalName());
                    if (stringMatcher != null) {
                        orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED, stringMatcher)));
                    }
                    continue;
                }

                if (Principal.IdentifierCase.HEADER == identifierCase) {
                    if (null == orId.getHeader() || null == orId.getHeader().getName()) {
                        continue;
                    }
                    String headerName = orId.getHeader().getName();
                    StringMatcher stringMatcher = MatcherUtil.convertHeaderMatcher(orId.getHeader());
                    orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.HEADER, headerName, stringMatcher)));
                    continue;
                }

                if (Principal.IdentifierCase.REMOTE_IP == identifierCase) {
                    if (null == orId.getRemoteIp()) {
                        continue;
                    }
                    orChildren.addChildren(new AuthRule(
                        new AuthCondition(AuthType.REMOTE_IP, MatcherUtil.convertIpMatcher(orId.getRemoteIp()))));
                    continue;
                }

                if (Principal.IdentifierCase.DIRECT_REMOTE_IP == identifierCase) {
                    if (null == orId.getDirectRemoteIp()) {
                        continue;
                    }
                    orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                        MatcherUtil.convertIpMatcher(orId.getDirectRemoteIp()))));
                    continue;

                }

                if (Principal.IdentifierCase.METADATA == identifierCase) {
                    if (null == orId.getMetadata() || null == orId.getMetadata().getPathList()) {
                        continue;
                    }
                    List<MetadataMatcher.PathSegment> segments = orId.getMetadata().getPathList();
                    String key = segments.get(0).getKey();
                    if (LDS_REQUEST_AUTH_PRINCIPAL.equals(key)) {
                        if (null == orId.getMetadata().getValue().getStringMatch()) {
                            continue;
                        }
                        StringMatcher stringMatcher = MatcherUtil.convStringMatcher(
                            orId.getMetadata().getValue().getStringMatch());
                        if (null != stringMatcher) {
                            orChildren.addChildren(
                                new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS, stringMatcher)));
                        }
                        continue;
                    }
                    if (LDS_REQUEST_AUTH_AUDIENCE.equals(key)) {
                        if (null == orId.getMetadata().getValue().getStringMatch()) {
                            continue;
                        }
                        StringMatcher stringMatcher = MatcherUtil.convStringMatcher(
                            orId.getMetadata().getValue().getStringMatch());
                        if (stringMatcher != null) {
                            orChildren.addChildren(
                                new AuthRule(new AuthCondition(AuthType.JWT_AUDIENCES, stringMatcher)));
                        }
                        continue;
                    }

                    if (LDS_REQUEST_AUTH_PRESENTER.equals(key)) {
                        if (null == orId.getMetadata().getValue().getStringMatch()) {
                            continue;
                        }
                        StringMatcher stringMatcher = MatcherUtil.convStringMatcher(
                            orId.getMetadata().getValue().getStringMatch());
                        if (stringMatcher != null) {
                            orChildren.addChildren(
                                new AuthRule(new AuthCondition(AuthType.JWT_PRESENTERS, stringMatcher)));
                        }
                        continue;
                    }
                    if (LDS_REQUEST_AUTH_CLAIMS.equals(key)) {
                        if (segments.size() >= 2) {
                            String matcherKey = segments.get(1).getKey();
                            StringMatcher stringMatcher = null;
                            try {
                                stringMatcher = MatcherUtil.convStringMatcher(
                                    orId.getMetadata().getValue().getListMatch()
                                        .getOneOf().getStringMatch());
                            } catch (Exception e) {
                                RecordLog.error("[XdsDataSource] Unable to get/convert request auth claims", e);
                                continue;
                            }
                            orChildren.addChildren(
                                new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, matcherKey, stringMatcher)));
                        }
                        continue;
                    }
                    RecordLog.warn("[XdsDataSource] Unsupported metadate type={}", key);
                }

                RecordLog.warn("[XdsDataSource] Unsupported identifierCase ={}", identifierCase);

            }
            if (!orChildren.isEmpty()) {
                andChildren.addChildren(orChildren);
            }
        }
        return andChildren;
    }

    private static AuthRule resolvePermission(Permission permission) {
        Permission.Set andRules = permission.getAndRules();
        AuthRule andChildren = new AuthRule(AuthRule.ChildChainType.AND);
        for (Permission andRule : andRules.getRulesList()) {
            if (andRule.getAny()) {
                return null;
            }
            //If not, go further
            boolean isNot = false;
            if (andRule.hasNotRule()) {
                isNot = true;
                andRule = andRule.getNotRule();
            }
            Permission.Set orRules = andRule.getOrRules();
            AuthRule orChildren = new AuthRule(AuthRule.ChildChainType.OR, isNot);
            for (Permission orRule : orRules.getRulesList()) {
                Permission.RuleCase rulecase = orRule.getRuleCase();

                if (Permission.RuleCase.DESTINATION_PORT == rulecase) {
                    int port = orRule.getDestinationPort();
                    if (0 != port) {
                        orChildren.addChildren(
                            new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT, new PortMatcher(port))));
                    }
                    continue;
                }
                if (Permission.RuleCase.REQUESTED_SERVER_NAME == rulecase) {

                    orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.REQUESTED_SERVER_NAME,
                        MatcherUtil.convStringMatcher(orRule.getRequestedServerName()))));
                    continue;
                }
                if (Permission.RuleCase.DESTINATION_IP == rulecase) {
                    if (null == orRule.getDestinationIp()) {
                        continue;
                    }
                    orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.DESTINATION_IP,
                        MatcherUtil.convertIpMatcher(orRule.getDestinationIp()))));
                    continue;
                }
                if (Permission.RuleCase.URL_PATH == rulecase) {
                    if (null == orRule || null == orRule.getUrlPath().getPath()) {
                        continue;
                    }
                    StringMatcher path = MatcherUtil.convStringMatcher(orRule.getUrlPath().getPath());
                    if (path != null) {
                        orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.URL_PATH, path)));
                    }
                }

                if (Permission.RuleCase.HEADER == rulecase) {
                    if (null == orRule.getHeader() || null == orRule.getHeader().getName()) {
                        continue;
                    }

                    String headerName = orRule.getHeader().getName();
                    if (LDS_HEADER_NAME_AUTHORITY.equals(headerName)) {
                        StringMatcher stringMatcher = MatcherUtil.convStringMatcher(orRule.getHeader());
                        if (stringMatcher != null) {
                            orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.HOSTS, stringMatcher)));
                        }
                        continue;
                    }

                    if (LDS_HEADER_NAME_METHOD.equals(headerName)) {
                        StringMatcher stringMatcher = MatcherUtil.convStringMatcher(orRule.getHeader());
                        if (stringMatcher != null) {
                            orChildren.addChildren(new AuthRule(new AuthCondition(AuthType.METHODS, stringMatcher)));
                        }
                        continue;
                    }
                    RecordLog.warn("[XdsDataSource] Header parsing is not supported, headerName={}", headerName);
                    continue;
                }
            }
            if (!orChildren.isEmpty()) {
                andChildren.addChildren(orChildren);
            }
        }
        return andChildren;
    }

    @Override
    public boolean resolve(List<Listener> listeners) {

        if (CollectionUtil.isEmpty(listeners)) {
            return false;
        }

        List<HttpFilter> httpFilters = resolveHttpFilter(listeners);
        Map<String, AuthRule> allowAuthRules = new HashMap<>();
        Map<String, AuthRule> denyAuthRules = new HashMap<>();
        resolveRbac(httpFilters, allowAuthRules, denyAuthRules);

        Map<String, JwtRule> jwtRules = resolveJWT(httpFilters);
        RecordLog.info("[XdsDataSource] Auth rules resolve finish, RBAC rules size: {}, Jwt rules size: {}",
            allowAuthRules.size() + denyAuthRules.size(), jwtRules.size());
        Rules rules = new Rules(allowAuthRules, denyAuthRules, jwtRules);
        authRepository.update(rules);
        return true;
    }

}
