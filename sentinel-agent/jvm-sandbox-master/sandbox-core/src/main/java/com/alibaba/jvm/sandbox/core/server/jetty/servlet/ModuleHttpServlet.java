package com.alibaba.jvm.sandbox.core.server.jetty.servlet;

import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.http.Http;
import com.alibaba.jvm.sandbox.core.CoreConfigure;
import com.alibaba.jvm.sandbox.core.CoreModule;
import com.alibaba.jvm.sandbox.core.CoreModule.ReleaseResource;
import com.alibaba.jvm.sandbox.core.manager.CoreModuleManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.matching;

/**
 * 用于处理模块的HTTP请求
 *
 * @author luanjia@taobao.com
 */
public class ModuleHttpServlet extends HttpServlet {
    private static final String SLASH = "/";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CoreConfigure cfg;
    private final CoreModuleManager coreModuleManager;

    public ModuleHttpServlet(final CoreConfigure cfg,
                             final CoreModuleManager coreModuleManager) {
        this.cfg = cfg;
        this.coreModuleManager = coreModuleManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding(cfg.getServerCharset().name());
        doMethod(req, resp, Http.Method.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding(cfg.getServerCharset().name());
        doMethod(req, resp, Http.Method.POST);
    }

    private void doMethod(final HttpServletRequest req,
                          final HttpServletResponse resp,
                          final Http.Method expectHttpMethod) throws ServletException, IOException {

        // 获取请求路径
        final String path = req.getPathInfo();

        // 获取模块ID
        final String uniqueId = parseUniqueId(path);
        if (StringUtils.isBlank(uniqueId)) {
            logger.warn("http request value={} was not found.", path);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 获取模块
        final CoreModule coreModule = coreModuleManager.get(uniqueId);
        if (null == coreModule) {
            logger.warn("module[id={}] was not existed, value={};", uniqueId, path);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 匹配对应的方法
        final Method method = matchingModuleMethod(
                path,
                expectHttpMethod,
                uniqueId,
                coreModule.getModule().getClass()
        );
        if (null == method) {
            logger.warn("module[id={};class={};] request method not found, value={};",
                    uniqueId,
                    coreModule.getModule().getClass().getName(),
                    path
            );
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            logger.debug("found method[class={};method={};] in module[id={};class={};]",
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    uniqueId,
                    coreModule.getModule().getClass().getName()
            );
        }

        // 生成方法调用参数
        final Object[] parameterObjectArray = generateParameterObjectArray(method, req, resp);

        final PrintWriter writer = coreModule.append(new ReleaseResource<PrintWriter>(resp.getWriter()) {

            @Override
            public void release() {
                IOUtils.closeQuietly(get());
            }

        });


        final boolean isAccessible = method.isAccessible();
        final ClassLoader oriThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            method.setAccessible(true);
            Thread.currentThread().setContextClassLoader(coreModule.getLoader());
            method.invoke(coreModule.getModule(), parameterObjectArray);
            logger.debug("http request value={} invoke module[id={};] {}#{} success.",
                    path, uniqueId, coreModule.getModule().getClass().getName(), method.getName());
        } catch (IllegalAccessException iae) {
            logger.warn("http request value={} invoke module[id={};] {}#{} occur access denied.",
                    path, uniqueId, coreModule.getModule().getClass().getName(), method.getName(), iae);
            throw new ServletException(iae);
        } catch (InvocationTargetException ite) {
            logger.warn("http request value={} invoke module[id={};] {}#{} failed.",
                    path, uniqueId, coreModule.getModule().getClass().getName(), method.getName(), ite.getTargetException());
            final Throwable targetCause = ite.getTargetException();
            if (targetCause instanceof ServletException) {
                throw (ServletException) targetCause;
            }
            if (targetCause instanceof IOException) {
                throw (IOException) targetCause;
            }
            throw new ServletException(targetCause);
        } finally {
            Thread.currentThread().setContextClassLoader(oriThreadContextClassLoader);
            method.setAccessible(isAccessible);
            coreModule.release(writer);
        }

    }


    /**
     * 提取模块ID
     * 模块ID应该在PATH的第一个位置
     *
     * @param path servlet访问路径
     * @return 路径解析成功则返回模块的ID，如果解析失败则返回null
     */
    private String parseUniqueId(final String path) {
        final String[] pathSegmentArray = StringUtils.split(path, "/");
        return ArrayUtils.getLength(pathSegmentArray) >= 1
                ? pathSegmentArray[0]
                : null;
    }


    /**
     * 匹配模块中复合HTTP请求路径的方法
     * 匹配方法的方式是：HttpMethod和HttpPath全匹配
     *
     * @param path          HTTP请求路径
     * @param httpMethod    HTTP请求方法
     * @param uniqueId      模块ID
     * @param classOfModule 模块类
     * @return 返回匹配上的方法，如果没有找到匹配方法则返回null
     */
    private Method matchingModuleMethod(final String path,
                                        final Http.Method httpMethod,
                                        final String uniqueId,
                                        final Class<?> classOfModule) {

        // 查找@Command注解的方法
        for (final Method method : MethodUtils.getMethodsListWithAnnotation(classOfModule, Command.class)) {
            final Command commandAnnotation = method.getAnnotation(Command.class);
            if (null == commandAnnotation) {
                continue;
            }
            // 兼容 value 是否以 / 开头的写法
            String cmd = appendSlash(commandAnnotation.value());
            final String pathOfCmd = "/" + uniqueId + cmd;
            if (StringUtils.equals(path, pathOfCmd)) {
                return method;
            }
        }
        // 查找@Http注解的方法
        for (final Method method : MethodUtils.getMethodsListWithAnnotation(classOfModule, Http.class)) {
            final Http httpAnnotation = method.getAnnotation(Http.class);
            if (null == httpAnnotation) {
                continue;
            }
            // 兼容 value 是否以 / 开头的写法
            String cmd = appendSlash(httpAnnotation.value());
            final String pathPattern = "/" + uniqueId + cmd;
            if (ArrayUtils.contains(httpAnnotation.method(), httpMethod)
                    && matching(path, pathPattern)) {
                return method;
            }
        }
        // 找不到匹配方法，返回null
        return null;
    }

    private String appendSlash(String cmd) {
        // 若不以 / 开头，则添加 /
        if (!cmd.startsWith(SLASH)) {
            cmd = SLASH + cmd;
        }
        return cmd;
    }

    private boolean isMapWithGenericParameterTypes(final Method method,
                                                   final int parameterIndex,
                                                   final Class<?> keyClass,
                                                   final Class<?> valueClass) {
        final Type[] genericParameterTypes = method.getGenericParameterTypes();
        if (genericParameterTypes.length < parameterIndex
                || !(genericParameterTypes[parameterIndex] instanceof ParameterizedType)) {
            return false;
        }
        final Type[] actualTypeArguments = ((ParameterizedType) genericParameterTypes[parameterIndex]).getActualTypeArguments();
        return actualTypeArguments.length == 2
                && keyClass.equals(actualTypeArguments[0])
                && valueClass.equals(actualTypeArguments[1]);
    }

    /**
     * 生成方法请求参数数组
     * 主要用于填充HttpServletRequest和HttpServletResponse
     *
     * @param method 模块Java方法
     * @param req    HttpServletRequest
     * @param resp   HttpServletResponse
     * @return 请求方法参数列表
     */
    private Object[] generateParameterObjectArray(final Method method,
                                                  final HttpServletRequest req,
                                                  final HttpServletResponse resp) throws IOException {

        final Class<?>[] parameterTypeArray = method.getParameterTypes();
        if (ArrayUtils.isEmpty(parameterTypeArray)) {
            return null;
        }
        final Object[] parameterObjectArray = new Object[parameterTypeArray.length];
        for (int index = 0; index < parameterObjectArray.length; index++) {
            final Class<?> parameterType = parameterTypeArray[index];

            // HttpServletRequest
            if (HttpServletRequest.class.isAssignableFrom(parameterType)) {
                parameterObjectArray[index] = req;
                continue;
            }

            // HttpServletResponse
            if (HttpServletResponse.class.isAssignableFrom(parameterType)) {
                parameterObjectArray[index] = resp;
                continue;
            }

            // ParameterMap<String,String[]>
            if (Map.class.isAssignableFrom(parameterType)
                    && isMapWithGenericParameterTypes(method, index, String.class, String[].class)) {
                parameterObjectArray[index] = req.getParameterMap();
            }

            // ParameterMap<String,String>
            else if (Map.class.isAssignableFrom(parameterType)
                    && isMapWithGenericParameterTypes(method, index, String.class, String.class)) {
                final Map<String, String> param = new HashMap<String, String>();
                for (final Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
                    param.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
                }
                parameterObjectArray[index] = param;
            }

            // QueryString
            else if (String.class.isAssignableFrom(parameterType)) {
                parameterObjectArray[index] = req.getQueryString();
            }


            // PrintWriter
            else if (PrintWriter.class.isAssignableFrom(parameterType)) {
                parameterObjectArray[index] = resp.getWriter();
            }


        }

        return parameterObjectArray;
    }

}
