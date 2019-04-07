package com.alibaba.csp.sentinel.dashboard.auth;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author cdfive
 * @since 1.6.0
 */
@Primary
@Component
public class SimpleWebAuthServiceImpl implements AuthService<HttpServletRequest> {

    public static final String WEB_SESSTION_KEY = "sentinel_admin";

    @Override
    public AuthUser getAuthUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object sentinelUserObj = session.getAttribute(SimpleWebAuthServiceImpl.WEB_SESSTION_KEY);
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
