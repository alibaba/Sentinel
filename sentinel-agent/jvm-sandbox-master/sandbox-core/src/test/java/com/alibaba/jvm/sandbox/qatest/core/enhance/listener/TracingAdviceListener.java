package com.alibaba.jvm.sandbox.qatest.core.enhance.listener;

import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.core.enhance.weaver.EventListenerHandlers;
import com.alibaba.jvm.sandbox.core.util.ObjectIDs;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassName;
import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassNameArray;
import static com.alibaba.jvm.sandbox.core.util.SandboxStringUtils.toJavaClassNameArray;
import static com.alibaba.jvm.sandbox.qatest.core.util.AssertUtils.assertArrayEquals;
import static com.sun.tools.javac.util.StringUtils.toUpperCase;
import static org.apache.commons.lang3.StringUtils.join;

public class TracingAdviceListener extends AdviceListener {

    public static final boolean IS_TOP = true;
    public static final boolean IS_NOT_TOP = false;

    private List<String> tracing = new ArrayList<String>();

    public static String generateTracing(final String prefix,
                                         final String javaClassName,
                                         final String javaMethodName,
                                         final String[] parameterTypes,
                                         final boolean isTop) {
        return String.format(
                "%s|%s.%s(%s)|%s",
                prefix,
                javaClassName,
                javaMethodName,
                join(parameterTypes, ","),
                toUpperCase(Boolean.toString(isTop))
        );
    }

    public static String generateTracing(final String prefix,
                                         final String javaClassName,
                                         final String javaMethodName,
                                         final String[] parameterTypes,
                                         final boolean isTop,
                                         final int callLineNum,
                                         final String callJavaClassName,
                                         final String callJavaMethodName,
                                         final String[] callParameterTypes) {
        return String.format(
                "%s|%s.%s(%s)|%s|%d|%s.%s(%s)",
                prefix,
                javaClassName,
                javaMethodName,
                join(parameterTypes, ","),
                toUpperCase(Boolean.toString(isTop)),
                callLineNum,
                callJavaClassName,
                callJavaMethodName,
                join(callParameterTypes, ",")
        );
    }

    public static String generateTracing(final String prefix,
                                         final String javaClassName,
                                         final String javaMethodName,
                                         final String[] parameterTypes,
                                         final boolean isTop,
                                         final int callLineNum,
                                         final String callJavaClassName,
                                         final String callJavaMethodName,
                                         final String[] callParameterTypes,
                                         final String callThrowJavaClassName) {
        return String.format(
                "%s|%s.%s(%s)|%s|%d|%s.%s(%s)|%s",
                prefix,
                javaClassName,
                javaMethodName,
                join(parameterTypes, ","),
                toUpperCase(Boolean.toString(isTop)),
                callLineNum,
                callJavaClassName,
                callJavaMethodName,
                join(callParameterTypes, ","),
                callThrowJavaClassName
        );
    }

    public static String generateTracing(final String prefix,
                                         final String javaClassName,
                                         final String javaMethodName,
                                         final String[] parameterTypes,
                                         final boolean isTop,
                                         final int lineNum) {
        return String.format(
                "%s|%s.%s(%s)|%s|%d|%s.%s(%s)|%s",
                prefix,
                javaClassName,
                javaMethodName,
                join(parameterTypes, ","),
                toUpperCase(Boolean.toString(isTop)),
                lineNum
        );
    }

    @Override
    protected void before(Advice advice) throws Throwable {
        tracing.add(generateTracing(
                "BEFORE",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop()
        ));
        super.before(advice);
    }

    @Override
    protected void afterReturning(Advice advice) throws Throwable {
        tracing.add(generateTracing(
                "RETURN",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop()
        ));
        super.afterReturning(advice);
    }

    @Override
    protected void after(Advice advice) throws Throwable {
        tracing.add(generateTracing(
                "AFTER",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop()
        ));
        super.after(advice);
    }

    @Override
    protected void afterThrowing(Advice advice) throws Throwable {
        tracing.add(generateTracing(
                "THROWING",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop()
        ));
        super.afterThrowing(advice);
    }

    @Override
    protected void beforeCall(Advice advice, int callLineNum, String callJavaClassName, String callJavaMethodName, String callJavaMethodDesc) {
        tracing.add(generateTracing(
                "CALL-BEFORE",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop(),
                callLineNum,
                callJavaClassName,
                callJavaMethodName,
                toJavaClassNameArray(Type.getMethodType(callJavaMethodDesc).getArgumentTypes())
        ));
        super.beforeCall(advice, callLineNum, callJavaClassName, callJavaMethodName, callJavaMethodDesc);
    }

    @Override
    protected void afterCallReturning(Advice advice, int callLineNum, String callJavaClassName, String callJavaMethodName, String callJavaMethodDesc) {
        tracing.add(generateTracing(
                "CALL-RETURN",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop(),
                callLineNum,
                callJavaClassName,
                callJavaMethodName,
                toJavaClassNameArray(Type.getMethodType(callJavaMethodDesc).getArgumentTypes())
        ));
        super.afterCallReturning(advice, callLineNum, callJavaClassName, callJavaMethodName, callJavaMethodDesc);
    }

    @Override
    protected void afterCall(Advice advice, int callLineNum, String callJavaClassName, String callJavaMethodName, String callJavaMethodDesc, String callThrowJavaClassName) {
        tracing.add(generateTracing(
                "CALL-AFTER",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop(),
                callLineNum,
                callJavaClassName,
                callJavaMethodName,
                toJavaClassNameArray(Type.getMethodType(callJavaMethodDesc).getArgumentTypes())
        ));
        super.afterCall(advice, callLineNum, callJavaClassName, callJavaMethodName, callJavaMethodDesc, callThrowJavaClassName);
    }

    @Override
    protected void afterCallThrowing(Advice advice, int callLineNum, String callJavaClassName, String callJavaMethodName, String callJavaMethodDesc, String callThrowJavaClassName) {
        tracing.add(generateTracing(
                "CALL-THROWS",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop(),
                callLineNum,
                callJavaClassName,
                callJavaMethodName,
                toJavaClassNameArray(Type.getMethodType(callJavaMethodDesc).getArgumentTypes()),
                callThrowJavaClassName
        ));
        super.afterCallThrowing(advice, callLineNum, callJavaClassName, callJavaMethodName, callJavaMethodDesc, callThrowJavaClassName);
    }

    @Override
    protected void beforeLine(Advice advice, int lineNum) {
        tracing.add(generateTracing(
                "CALL-THROWS",
                getJavaClassName(advice.getBehavior().getDeclaringClass()),
                advice.getBehavior().getName(),
                getJavaClassNameArray(advice.getBehavior().getParameterTypes()),
                advice.isProcessTop(),
                lineNum
        ));
        super.beforeLine(advice, lineNum);
    }

    public List<String> getTracing() {
        return tracing;
    }

    private EventListener eventListener;

    TracingAdviceListener setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    /**
     * 断言跟踪信息
     *
     * @param exceptTracings 期待的跟踪信息
     */
    public void assertTracing(final String... exceptTracings) {
        assertEventProcessor();
        assertArrayEquals(
                exceptTracings,
                getTracing().toArray(new String[]{})
        );
    }

    // 检查内核事件处理器是否正确
    private void assertEventProcessor() {
        EventListenerHandlers
                .getSingleton()
                .checkEventProcessor(ObjectIDs.instance.identity(eventListener));
    }

}
