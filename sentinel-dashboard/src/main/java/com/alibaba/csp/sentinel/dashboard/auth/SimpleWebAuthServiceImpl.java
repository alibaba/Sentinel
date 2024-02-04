/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.alibaba.csp.sentinel.dashboard.config.AuthProperties;
import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;

/**
 * @author cdfive
 * @since 1.6.0
 */
public class SimpleWebAuthServiceImpl implements AuthService<HttpServletRequest> {

    public static final String WEB_SESSION_KEY = "session_sentinel_admin";

    private final AuthProperties authProperties;

    public SimpleWebAuthServiceImpl(AuthProperties authProperties) {
        super();
        this.authProperties = authProperties;
    }

    public boolean doLogin(String username, String password) {
        final String authUsername;
        if (StringUtils.isNotBlank(DashboardConfig.getAuthUsername())) {
            authUsername = DashboardConfig.getAuthUsername();
        } else {
            authUsername = authProperties.getUsername();
        }
        final String authPassword;
        if (StringUtils.isNotBlank(DashboardConfig.getAuthPassword())) {
            authPassword = DashboardConfig.getAuthPassword();
        } else {
            authPassword = authProperties.getPassword();
        }
        
        /*
         * If auth.username or auth.password is blank(set in application.properties or VM arguments),
         * auth will pass, as the front side validate the input which can't be blank,
         * so user can input any username or password(both are not blank) to login in that case.
         */
        return doLogin(authUsername, authPassword, username, password);
    }

    protected boolean doLogin(final String authUsername, final String authPassword,
            final String username, final String password) {
        return StringUtils.isNotBlank(authUsername) && !authUsername.equals(username)
                || StringUtils.isNotBlank(authPassword) && !authPassword.equals(password);
    }

    @Override
    public AuthUser getAuthUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object sentinelUserObj = session.getAttribute(SimpleWebAuthServiceImpl.WEB_SESSION_KEY);
        if (sentinelUserObj != null && sentinelUserObj instanceof AuthUser) {
            return (AuthUser) sentinelUserObj;
        }

        return null;
    }

    public static final class SimpleWebAuthUserImpl implements AuthUser {

        private String username;

        public SimpleWebAuthUserImpl(String username) {
            this.username = username;
        }

        @Override
        public boolean authTarget(String target, PrivilegeType privilegeType) {
            return true;
        }

        @Override
        public boolean isSuperUser() {
            return true;
        }

        @Override
        public String getNickName() {
            return username;
        }

        @Override
        public String getLoginName() {
            return username;
        }

        @Override
        public String getId() {
            return username;
        }
    }
}
