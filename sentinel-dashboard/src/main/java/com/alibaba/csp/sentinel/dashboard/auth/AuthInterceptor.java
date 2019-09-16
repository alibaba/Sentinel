package com.alibaba.csp.sentinel.dashboard.auth;

import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            Method method = ((HandlerMethod) handler).getMethod();

            AuthAction authAction = method.getAnnotation(AuthAction.class);
            if (authAction != null) {
                AuthService.AuthUser authUser = authService.getAuthUser(request);
                if (authUser == null) {
                    responseNoPrivilegeMsg(response, authAction.message());
                    return false;
                }
                String target = request.getParameter(authAction.targetName());

                if (authUser.authTarget(target, authAction.value())) {
                    responseNoPrivilegeMsg(response, authAction.message());
                    return false;
                }
            }
        }

        return true;
    }

    private void responseNoPrivilegeMsg(HttpServletResponse response, String message) throws IOException {
        Result result = Result.ofFail(-1, message);
        response.addHeader("Content-Type", "application/json;charset=UTF-8");
        response.getOutputStream().write(JSON.toJSONBytes(result));
    }
}
