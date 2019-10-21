package com.alibaba.jvm.sandbox.core.enhance.weaver.asm;

import org.objectweb.asm.commons.Method;

import java.com.alibaba.jvm.sandbox.spy.Spy;

import static com.alibaba.jvm.sandbox.core.enhance.weaver.asm.AsmMethods.InnerHelper.getAsmMethod;
import static com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils.unCaughtGetClassDeclaredJavaMethod;

/**
 * 常用的ASM method 集合
 * 省得我到处声明
 * Created by luanjia@taobao.com on 16/5/21.
 */
public interface AsmMethods {

    class InnerHelper {
        private InnerHelper() {
        }

        static Method getAsmMethod(final Class<?> clazz,
                                   final String methodName,
                                   final Class<?>... parameterClassArray) {
            return Method.getMethod(unCaughtGetClassDeclaredJavaMethod(clazz, methodName, parameterClassArray));
        }
    }

    /**
     * asm method of {@link Spy#spyMethodOnBefore(Object[], String, int, int, String, String, String, Object)}
     */
    Method ASM_METHOD_Spy$spyMethodOnBefore = getAsmMethod(
            Spy.class,
            "spyMethodOnBefore",
            Object[].class, String.class, int.class, int.class, String.class, String.class, String.class, Object.class
    );

    /**
     * asm method of {@link Spy#spyMethodOnReturn(Object, String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnReturn = getAsmMethod(
            Spy.class,
            "spyMethodOnReturn",
            Object.class, String.class, int.class
    );

    /**
     * asm method of {@link Spy#spyMethodOnThrows(Throwable, String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnThrows = getAsmMethod(
            Spy.class,
            "spyMethodOnThrows",
            Throwable.class, String.class, int.class
    );


    /**
     * asm method of {@link Spy#spyMethodOnLine(int, String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnLine = getAsmMethod(
            Spy.class,
            "spyMethodOnLine",
            int.class, String.class, int.class
    );

    /**
     * asm method of {@link Spy#spyMethodOnCallBefore(int, String, String, String, String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnCallBefore = getAsmMethod(
            Spy.class,
            "spyMethodOnCallBefore",
            int.class, String.class, String.class, String.class, String.class, int.class
    );

    /**
     * asm method of {@link Spy#spyMethodOnCallReturn(String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnCallReturn = getAsmMethod(
            Spy.class,
            "spyMethodOnCallReturn",
            String.class, int.class
    );

    /**
     * asm method of {@link Spy#spyMethodOnCallThrows(String, String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnCallThrows = getAsmMethod(
            Spy.class,
            "spyMethodOnCallThrows",
            String.class, String.class, int.class
    );

//    /**
//     * asm method of {@link Class#forName(String)}
//     */
//    Method ASM_METHOD_Class$forName = getAsmMethod(
//            Class.class,
//            "forName",
//            String.class
//    );

    Method ASM_METHOD_Class$getName = getAsmMethod(
            Class.class,
            "getName"
    );

    /**
     * asm method of {@link Class#getClass()}
     */
    Method ASM_METHOD_Object$getClass = getAsmMethod(
            Object.class,
            "getClass"
    );

//    /**
//     * asm method of {@link Class#getClassLoader()}
//     */
//    Method ASM_METHOD_Class$getClassLoader = getAsmMethod(
//            Class.class,
//            "getClassLoader"
//    );


}
