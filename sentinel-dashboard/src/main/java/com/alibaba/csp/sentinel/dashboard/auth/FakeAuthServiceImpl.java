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

import org.springframework.stereotype.Component;

/**
 * A fake AuthService implementation, which will pass all user auth checking.
 *
 * @author Carpenter Lee
 * @since 1.5.0
 */
@Component
public class FakeAuthServiceImpl implements AuthService<HttpServletRequest> {

    @Override
    public AuthUser getAuthUser(HttpServletRequest request) {
        return new AuthUserImpl();
    }

    static final class AuthUserImpl implements AuthUser {

        @Override
        public boolean authTarget(String target, PrivilegeType privilegeType) {
            // fake implementation, always return true
            return true;
        }

        @Override
        public boolean isSuperUser() {
            // fake implementation, always return true
            return true;
        }

        @Override
        public String getNickName() {
            return "FAKE_NICK_NAME";
        }

        @Override
        public String getLoginName() {
            return "FAKE_LOGIN_NAME";
        }

        @Override
        public String getId() {
            return "FAKE_EMP_ID";
        }
    }
}
