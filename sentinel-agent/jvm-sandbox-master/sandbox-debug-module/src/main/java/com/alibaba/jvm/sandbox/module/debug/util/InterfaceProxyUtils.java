package com.alibaba.jvm.sandbox.module.debug.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassName;
import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassNameArray;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.deepEquals;

public class InterfaceProxyUtils {

    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface ProxyMethod {

        /**
         * 目标方法名
         *
         * @return 目标方法名
         */
        String name();

    }

    /**
     * 用于包装目标对象操作的代理方法处理
     */
    static abstract class WrapInvocationHandler implements InvocationHandler {

        final Map<Method, Method> mappingOfWrapMethods = new ConcurrentHashMap<Method, Method>();

        String getInterfaceMethodName(final Method interfaceMethod) {
            final ProxyMethod proxyMethod = interfaceMethod.getAnnotation(ProxyMethod.class);
            return null == proxyMethod
                    ? interfaceMethod.getName()
                    : proxyMethod.name();
        }

        /**
         * 比较interfaceMethod和targetMethod两个方法是否接近
         *
         * @param interfaceMethod 接口声明的方法
         * @param targetMethod    目标对象声明的方法
         * @return TRUE:接近;FALSE:不接近
         */
        boolean isCloseTo(final Method interfaceMethod, final Method targetMethod) {
            return StringUtils.equals(getInterfaceMethodName(interfaceMethod), targetMethod.getName())
                    && deepEquals(getJavaClassNameArray(interfaceMethod.getParameterTypes()), getJavaClassNameArray(targetMethod.getParameterTypes()));
        }

        Method getTargetMethod(final Method interfaceMethod, final Object target) throws NoSuchMethodException {
            if (mappingOfWrapMethods.containsKey(interfaceMethod)) {
                return mappingOfWrapMethods.get(interfaceMethod);
            }
            for (final Method targetMethod : target.getClass().getMethods()) {
                if (isCloseTo(interfaceMethod, targetMethod)) {
                    mappingOfWrapMethods.put(interfaceMethod, targetMethod);
                    return targetMethod;
                }
            }
            throw new NoSuchMethodException(String.format("%s.%s(%s) method not found!",
                    getJavaClassName(target.getClass()),
                    getInterfaceMethodName(interfaceMethod),
                    StringUtils.join(getJavaClassNameArray(interfaceMethod.getParameterTypes()), ",")
            ));
        }

    }

    /**
     * 构造一个接口的实现傀儡类，用接口去调用目标类
     *
     * @param interfaceClass 目标接口
     * @param target         傀儡类实例
     * @param <T>            目标接口类型
     * @return 被目标接口操纵的傀儡对象实例
     */
    public static <T> T puppet(final Class<T> interfaceClass,
                               final Object target) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new WrapInvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method interfaceMethod, Object[] args) throws Throwable {
                        return getTargetMethod(interfaceMethod, target).invoke(target, args);
                    }

                }
        );
    }


    public interface MethodInterceptor {
        Object invoke(MethodInvocation methodInvocation) throws Throwable;
    }

    public interface MethodInvocation {
        Method getMethod();

        Object[] getArguments();

        Object proceed() throws Throwable;

        Object getThis();

        AccessibleObject getStaticPart();
    }

    /**
     * 拦截目标类的方法
     *
     * @param interfaceClassInTargetClassLoader 目标接口
     * @param targetClassLoader                 目标对象所在ClassLoader
     * @param target                            目标对象实例
     * @param interceptor                       拦截器
     * @return 带拦截器的目标对象实例
     */
    public static Object intercept(final Class<?> interfaceClassInTargetClassLoader,
                                   final ClassLoader targetClassLoader,
                                   final Object target,
                                   final MethodInterceptor interceptor) {
        return Proxy.newProxyInstance(
                targetClassLoader,
                new Class<?>[]{interfaceClassInTargetClassLoader},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                        return interceptor.invoke(new MethodInvocation() {
                            @Override
                            public Method getMethod() {
                                return method;
                            }

                            @Override
                            public Object[] getArguments() {
                                return args;
                            }

                            @Override
                            public Object proceed() throws Throwable {
                                return method.invoke(target, args);
                            }

                            @Override
                            public Object getThis() {
                                return target;
                            }

                            @Override
                            public AccessibleObject getStaticPart() {
                                return method;
                            }
                        });
                    }
                }
        );
    }

}
