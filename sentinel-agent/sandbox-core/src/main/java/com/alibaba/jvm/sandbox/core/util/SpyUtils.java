package com.alibaba.jvm.sandbox.core.util;

import com.alibaba.jvm.sandbox.core.enhance.weaver.EventListenerHandlers;

import java.com.alibaba.jvm.sandbox.spy.Spy;

import static com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils.unCaughtGetClassDeclaredJavaMethod;

/**
 * Spy类操作工具类
 *
 * @author luajia@taobao.com
 */
public class SpyUtils {


    /**
     * 初始化Spy类
     *
     * @param namespace 命名空间
     */
    public synchronized static void init(final String namespace) {

        if (Spy.isInit(namespace)) {
            return;
        }

        Spy.init(
                namespace,
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onBefore",
                        int.class,
                        int.class,
                        Class.class,
                        String.class,
                        String.class,
                        String.class,
                        Object.class,
                        Object[].class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onReturn",
                        int.class,
                        Class.class,
                        Object.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onThrows",
                        int.class,
                        Class.class,
                        Throwable.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onLine",
                        int.class,
                        int.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onCallBefore",
                        int.class,
                        int.class,
                        String.class,
                        String.class,
                        String.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onCallReturn",
                        int.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onCallThrows",
                        int.class,
                        String.class
                )
        );

    }

    /**
     * 清理Spy中的命名空间
     *
     * @param namespace 命名空间
     */
    public synchronized static void clean(final String namespace) {
        Spy.clean(namespace);
    }

}
