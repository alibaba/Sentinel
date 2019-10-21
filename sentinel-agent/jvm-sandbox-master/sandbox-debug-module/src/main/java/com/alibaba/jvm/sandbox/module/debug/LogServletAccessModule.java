package com.alibaba.jvm.sandbox.module.debug;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.LoadCompleted;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.alibaba.jvm.sandbox.module.debug.util.InterfaceProxyUtils.MethodInterceptor;
import com.alibaba.jvm.sandbox.module.debug.util.InterfaceProxyUtils.MethodInvocation;
import com.alibaba.jvm.sandbox.module.debug.util.InterfaceProxyUtils.ProxyMethod;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.alibaba.jvm.sandbox.module.debug.util.InterfaceProxyUtils.intercept;
import static com.alibaba.jvm.sandbox.module.debug.util.InterfaceProxyUtils.puppet;
import static org.apache.commons.lang3.ArrayUtils.contains;

/**
 * 基于HTTP-SERVLET(v2.4)规范的HTTP访问日志
 *
 * @author luanjia@taobao.com
 */
@MetaInfServices(Module.class)
@Information(id = "debug-servlet-access", version = "0.0.2", author = "luanjia@taobao.com")
public class LogServletAccessModule implements Module, LoadCompleted {

    private final Logger logger = LoggerFactory.getLogger("DEBUG-SERVLET-ACCESS");

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    /**
     * HTTP接入信息
     */
    static class HttpAccess {
        final long beginTimestamp = System.currentTimeMillis();
        final String from;
        final String method;
        final String uri;
        final Map<String, String[]> parameterMap;
        final String userAgent;
        int status = 200;

        HttpAccess(final String from,
                   final String method,
                   final String uri,
                   final Map<String, String[]> parameterMap,
                   final String userAgent) {
            this.from = from;
            this.method = method;
            this.uri = uri;
            this.parameterMap = parameterMap;
            this.userAgent = userAgent;
        }

        void setStatus(int status) {
            this.status = status;
        }

    }

    interface IHttpServletRequest {

        @ProxyMethod(name = "getRemoteAddr")
        String getRemoteAddress();

        String getMethod();

        String getRequestURI();

        Map<String, String[]> getParameterMap();

        String getHeader(String name);

    }

    @Override
    public void loadCompleted() {
        new EventWatchBuilder(moduleEventWatcher)
                .onClass("javax.servlet.http.HttpServlet")
                .includeSubClasses()
                .onBehavior("service")
                .withParameterTypes(
                        "javax.servlet.http.HttpServletRequest",
                        "javax.servlet.http.HttpServletResponse"
                )
                .onWatch(new AdviceListener() {

                    @Override
                    protected void before(Advice advice) throws Throwable {


                        // 只关心顶层调用
                        if (!advice.isProcessTop()) {
                            return;
                        }

                        // 俘虏HttpServletRequest参数为傀儡
                        final IHttpServletRequest httpServletRequest = puppet(
                                IHttpServletRequest.class,
                                advice.getParameterArray()[0]
                        );

                        // 初始化HttpAccess
                        final HttpAccess httpAccess = new HttpAccess(
                                httpServletRequest.getRemoteAddress(),
                                httpServletRequest.getMethod(),
                                httpServletRequest.getRequestURI(),
                                httpServletRequest.getParameterMap(),
                                httpServletRequest.getHeader("User-Agent")
                        );

                        // 附加到advice上，以便在onReturning()和onThrowing()中取出
                        advice.attach(httpAccess);

                        final Class<?> classOfHttpServletResponse = advice.getBehavior()
                                .getDeclaringClass()
                                .getClassLoader()
                                .loadClass("javax.servlet.http.HttpServletResponse");

                        // 替换HttpServletResponse参数
                        advice.changeParameter(1, intercept(
                                classOfHttpServletResponse,
                                advice.getTarget().getClass().getClassLoader(),
                                advice.getParameterArray()[1],
                                new MethodInterceptor() {
                                    @Override
                                    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                                        if (contains(
                                                new String[]{
                                                        "setStatus",
                                                        "sendError"
                                                },
                                                methodInvocation.getMethod().getName())) {
                                            httpAccess.setStatus((Integer) methodInvocation.getArguments()[0]);
                                        }
                                        return methodInvocation.proceed();
                                    }
                                }));

                    }

                    @Override
                    protected void afterReturning(Advice advice) {
                        // 只关心顶层调用
                        if (!advice.isProcessTop()) {
                            return;
                        }

                        final HttpAccess httpAccess = advice.attachment();
                        if (null == httpAccess) {
                            return;
                        }

                        logAccess(
                                httpAccess,
                                System.currentTimeMillis() - httpAccess.beginTimestamp,
                                null
                        );
                    }

                    @Override
                    protected void afterThrowing(Advice advice) {
                        // 只关心顶层调用
                        if (!advice.isProcessTop()) {
                            return;
                        }

                        final HttpAccess httpAccess = advice.attachment();
                        if (null == httpAccess) {
                            return;
                        }

                        logAccess(
                                httpAccess,
                                System.currentTimeMillis() - httpAccess.beginTimestamp,
                                advice.getThrowable()
                        );
                    }

                });

    }

    // 格式化ParameterMap
    private static String formatParameterMap(final Map<String, String[]> parameterMap) {
        if (MapUtils.isEmpty(parameterMap)) {
            return StringUtils.EMPTY;
        }
        final Set<String> kvPairs = new LinkedHashSet<String>();
        for (final Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            kvPairs.add(String.format("%s=%s",
                    entry.getKey(),
                    StringUtils.join(entry.getValue(), ",")
            ));
        }
        return StringUtils.join(kvPairs, "&");
    }


    /*
     * 记录access日志
     */
    private void logAccess(final HttpAccess ha,
                           final long costMs,
                           final Throwable cause) {
        logger.info("{};{};{};{}ms;{};[{}];{};",
                ha.from,
                ha.status,
                ha.method,
                costMs,
                ha.uri,
                formatParameterMap(ha.parameterMap),
                ha.userAgent,
                cause
        );
    }

}
