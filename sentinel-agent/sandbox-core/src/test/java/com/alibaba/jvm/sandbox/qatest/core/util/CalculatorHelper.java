package com.alibaba.jvm.sandbox.qatest.core.util;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.api.event.ThrowsEvent;
import com.alibaba.jvm.sandbox.api.filter.Filter;
import com.alibaba.jvm.sandbox.api.filter.NameRegexFilter;
import com.alibaba.jvm.sandbox.core.util.UnCaughtException;
import com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator;
import com.alibaba.jvm.sandbox.qatest.core.enhance.target.MyCalculator;

import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassName;
import static com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils.unCaughtGetClassDeclaredJavaMethod;
import static com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils.unCaughtInvokeMethod;

/**
 * 计算器辅助类
 */
public class CalculatorHelper {

    public static final String CALCULATOR_CLASS_NAME = getJavaClassName(Calculator.class);
    public static final String MY_CALCULATOR_CLASS_NAME = getJavaClassName(MyCalculator.class);

    /**
     * 拦截sum()方法过滤器
     */
    public static final Filter CALCULATOR_SUM_FILTER
            = new NameRegexFilter(
            "^com\\.alibaba\\.jvm.sandbox\\.qatest\\.core\\.enhance\\.target\\.Calculator$",
            "^sum$"
    );

    /**
     * 拦截sum()和add()方法过滤器
     */
    public static final Filter CALCULATOR_SUM_and_ADD_FILTER
            = new NameRegexFilter(
            "^com\\.alibaba\\.jvm.sandbox\\.qatest\\.core\\.enhance\\.target\\.Calculator$",
            "^(sum|add)$"
    );

    /**
     * 拦截errorSum()方法过滤器
     */
    public static final Filter CALCULATOR_ERROR_SUM_FILTER
            = new NameRegexFilter(
            "^com\\.alibaba\\.jvm.sandbox\\.qatest\\.core\\.enhance\\.target\\.Calculator$",
            "^errorSum$"
    );

    /**
     * 拦截pow()方法过滤器
     */
    public static final Filter CALCULATOR_POW_FILTER
            = new NameRegexFilter(
            "^com\\.alibaba\\.jvm.sandbox\\.qatest\\.core\\.enhance\\.target\\.Calculator$",
            "^pow"
    );

    /**
     * 拦截初始化方法过滤器
     */
    public static final Filter CALCULATOR_INIT_FILTER
            = new NameRegexFilter(
            "^com\\.alibaba\\.jvm.sandbox\\.qatest\\.core\\.enhance\\.target\\.Calculator$",
            "<init>"
    );

    /**
     * 拦截sum()方法过滤器
     */
    public static final Filter MY_CALCULATOR_SUM_FILTER
            = new NameRegexFilter(
            "^com\\.alibaba\\.jvm.sandbox\\.qatest\\.core\\.enhance\\.target\\.MyCalculator$",
            "^sum$"
    );

    public static final Filter CALCULATOR_INIT_FILTER_WITH_TEST_CASE
            = new Filter() {
        @Override
        public boolean doClassFilter(int access, String javaClassName, String superClassTypeJavaClassName, String[] interfaceTypeJavaClassNameArray, String[] annotationTypeJavaClassNameArray) {
            return javaClassName.equalsIgnoreCase("com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator");
        }

        @Override
        public boolean doMethodFilter(int access, String javaMethodName, String[] parameterTypeJavaClassNameArray, String[] throwsTypeJavaClassNameArray, String[] annotationTypeJavaClassNameArray) {
            if (javaMethodName.equalsIgnoreCase("<init>"))
                return (parameterTypeJavaClassNameArray.length == 1
                        && parameterTypeJavaClassNameArray[0].equalsIgnoreCase("com.alibaba.jvm.sandbox.qatest.core.enhance.target.Calculator$TestCase"));
            return false;
        }
    };

    // 栈
    public static Stack<Event> stack = new Stack<Event>();

    /**
     * 判断是否是指定方法的Return||Throws事件
     *
     * @param event          事件
     * @param javaMethodName 触发事件的方法名
     * @return
     */
    public static boolean isSpecialMethodEvent(Event event, String javaMethodName) {

        if (event instanceof BeforeEvent) {
            BeforeEvent beforeEvent = new BeforeEvent(
                    ((BeforeEvent) event).processId,
                    ((BeforeEvent) event).invokeId,
                    ((BeforeEvent) event).javaClassLoader,
                    ((BeforeEvent) event).javaClassName,
                    ((BeforeEvent) event).javaMethodName,
                    ((BeforeEvent) event).javaMethodDesc,
                    ((BeforeEvent) event).target,
                    ((BeforeEvent) event).argumentArray);
            stack.push(beforeEvent);
        }
        if (event instanceof ReturnEvent) {
            Event eventEnd = stack.pop();
            return isSpecialMethod(eventEnd, javaMethodName);
        }

        if (event instanceof ThrowsEvent) {
            Event eventEnd = stack.pop();
            return isSpecialMethod(eventEnd, javaMethodName);
        }
        return false;
    }

    public static boolean isSpecialMethod(Event event, String javaMethodName) {
        if (event instanceof BeforeEvent) {
            BeforeEvent beforeEvent = (BeforeEvent) event;
            return beforeEvent.javaMethodName.equalsIgnoreCase(javaMethodName);
        }
        return false;
    }

    /**
     * 调用sum()方法
     *
     * @param calculatorObject 目标计算器对象实例
     * @param numArray         参数
     * @return 返回值
     * @throws Throwable 调用失败
     */
    public static int sum(final Object calculatorObject, int... numArray) throws Throwable {
        try {
            return unCaughtInvokeMethod(
                    unCaughtGetClassDeclaredJavaMethod(calculatorObject.getClass(), "sum", int[].class),
                    calculatorObject,
                    numArray
            );
        } catch (Throwable cause) {
            if (cause instanceof UnCaughtException
                    && (cause.getCause() instanceof InvocationTargetException)) {
                throw ((InvocationTargetException) cause.getCause()).getTargetException();
            }
            throw cause;
        }

    }

    /**
     * 调用sum()方法
     *
     * @param calculatorObject pow();
     * @param num              num
     * @param n                n次方
     * @return 返回值
     * @throws Throwable 调用失败
     */
    public static int pow(final Object calculatorObject, int num, int n) throws Throwable {
        try {
            return unCaughtInvokeMethod(
                    unCaughtGetClassDeclaredJavaMethod(calculatorObject.getClass(), "pow", int.class, int.class),
                    calculatorObject,
                    num,
                    n
            );
        } catch (Throwable cause) {
            if (cause instanceof UnCaughtException
                    && (cause.getCause() instanceof InvocationTargetException)) {
                throw ((InvocationTargetException) cause.getCause()).getTargetException();
            }
            throw cause;
        }
    }

    public static Object newInstance(final Class<?> calculatorClass) throws Throwable {
        try {
            return calculatorClass.getConstructor().newInstance();
        } catch (InvocationTargetException cause) {
            throw cause.getTargetException();
        }
    }

    public static Object newInstance(final Class<?> calculatorClass, final Calculator.TestCase tCase) throws Throwable {
        try {
            return calculatorClass.getConstructor(String.class).newInstance(tCase.name());
        } catch (InvocationTargetException cause) {
            throw cause.getTargetException();
        }
    }

}
