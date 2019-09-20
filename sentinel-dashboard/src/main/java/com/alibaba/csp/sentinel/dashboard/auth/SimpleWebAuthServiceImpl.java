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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author cdfive
 * @since 1.6.0
 */
@Component
@Primary
@ConditionalOnProperty(name = "auth.enabled", matchIfMissing = true)
public class SimpleWebAuthServiceImpl implements AuthService<HttpServletRequest> {

    public static final String WEB_SESSION_KEY = "session_sentinel_admin";

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
