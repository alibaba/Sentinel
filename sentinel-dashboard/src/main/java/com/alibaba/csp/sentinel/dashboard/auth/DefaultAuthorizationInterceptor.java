package com.alibaba.csp.sentinel.dashboard.auth;

import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.fastjson.JSON;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * The web interceptor for privilege-based authorization.
 * <p>
 * move from old {@link AuthorizationInterceptor}.
 *
 * @author lkxiaolou
 * @author wxq
 * @since 1.7.1
 */
public class DefaultAuthorizationInterceptor implements AuthorizationInterceptor {

    private final AuthService<HttpServletRequest> authService;

    public DefaultAuthorizationInterceptor(AuthService<HttpServletRequest> authService) {
        this.authService = authService;
    }

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

                if (!authUser.authTarget(target, authAction.value())) {
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
