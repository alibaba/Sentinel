package com.alibaba.jvm.sandbox.core.enhance.weaver;

import com.alibaba.jvm.sandbox.api.event.*;
import com.alibaba.jvm.sandbox.core.util.UnsafeUtils;
import sun.misc.Unsafe;

import static com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils.unCaughtSetClassDeclaredJavaFieldValue;

/**
 * 单例事件工厂
 */
class SingleEventFactory {

    private static final int ILLEGAL_PROCESS_ID = -1;
    private static final int ILLEGAL_INVOKE_ID = -1;

    private static final Unsafe unsafe;
    private static final long processIdFieldInInvokeEventOffset;
    private static final long invokeIdFieldInInvokeEventOffset;
    private static final long javaClassLoaderFieldInBeforeEventOffset;
    private static final long javaClassNameFieldInBeforeEventOffset;
    private static final long javaMethodNameFieldInBeforeEventOffset;
    private static final long javaMethodDescFieldInBeforeEventOffset;
    private static final long targetFieldInBeforeEventOffset;
    private static final long argumentArrayFieldInBeforeEventOffset;
    private static final long objectFieldInReturnEventOffset;
    private static final long throwableFieldInThrowsEventOffset;
    private static final long lineNumberFieldInLineEventOffset;

    private static final long lineNumberFieldInCallBeforeEventOffset;
    private static final long ownerFieldInCallBeforeEventOffset;
    private static final long nameFieldInCallBeforeEventOffset;
    private static final long descFieldInCallBeforeEventOffset;
    private static final long throwExceptionFieldInCallThrowsEventOffset;

    static {
        try {
            unsafe = UnsafeUtils.getUnsafe();
            processIdFieldInInvokeEventOffset = unsafe.objectFieldOffset(InvokeEvent.class.getDeclaredField("processId"));
            invokeIdFieldInInvokeEventOffset = unsafe.objectFieldOffset(InvokeEvent.class.getDeclaredField("invokeId"));
            javaClassLoaderFieldInBeforeEventOffset = unsafe.objectFieldOffset(BeforeEvent.class.getDeclaredField("javaClassLoader"));
            javaClassNameFieldInBeforeEventOffset = unsafe.objectFieldOffset(BeforeEvent.class.getDeclaredField("javaClassName"));
            javaMethodNameFieldInBeforeEventOffset = unsafe.objectFieldOffset(BeforeEvent.class.getDeclaredField("javaMethodName"));
            javaMethodDescFieldInBeforeEventOffset = unsafe.objectFieldOffset(BeforeEvent.class.getDeclaredField("javaMethodDesc"));
            targetFieldInBeforeEventOffset = unsafe.objectFieldOffset(BeforeEvent.class.getDeclaredField("target"));
            argumentArrayFieldInBeforeEventOffset = unsafe.objectFieldOffset(BeforeEvent.class.getDeclaredField("argumentArray"));
            objectFieldInReturnEventOffset = unsafe.objectFieldOffset(ReturnEvent.class.getDeclaredField("object"));
            throwableFieldInThrowsEventOffset = unsafe.objectFieldOffset(ThrowsEvent.class.getDeclaredField("throwable"));
            lineNumberFieldInLineEventOffset = unsafe.objectFieldOffset(LineEvent.class.getDeclaredField("lineNumber"));

            lineNumberFieldInCallBeforeEventOffset = unsafe.objectFieldOffset(CallBeforeEvent.class.getDeclaredField("lineNumber"));
            ownerFieldInCallBeforeEventOffset = unsafe.objectFieldOffset(CallBeforeEvent.class.getDeclaredField("owner"));
            nameFieldInCallBeforeEventOffset = unsafe.objectFieldOffset(CallBeforeEvent.class.getDeclaredField("name"));
            descFieldInCallBeforeEventOffset = unsafe.objectFieldOffset(CallBeforeEvent.class.getDeclaredField("desc"));
            throwExceptionFieldInCallThrowsEventOffset = unsafe.objectFieldOffset(CallThrowsEvent.class.getDeclaredField("throwException"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private LineEvent lineEvent = null;
    private BeforeEvent beforeEvent = null;
    private ReturnEvent returnEvent = null;
    private ThrowsEvent throwsEvent = null;
    private CallBeforeEvent callBeforeEvent = null;
    private CallReturnEvent callReturnEvent = null;
    private CallThrowsEvent callThrowsEvent = null;
    private ImmediatelyThrowsEvent immediatelyThrowsEvent = null;
    private ImmediatelyReturnEvent immediatelyReturnEvent = null;


    public BeforeEvent makeBeforeEvent(final int processId,
                                       final int invokeId,
                                       final ClassLoader javaClassLoader,
                                       final String javaClassName,
                                       final String javaMethodName,
                                       final String javaMethodDesc,
                                       final Object target,
                                       final Object[] argumentArray) {
        if (null == beforeEvent) {
            beforeEvent = new BeforeEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, null, null, null, null, null, null);
        }
        unsafe.putInt(beforeEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(beforeEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putObject(beforeEvent, javaClassLoaderFieldInBeforeEventOffset, javaClassLoader);
        unsafe.putObject(beforeEvent, javaClassNameFieldInBeforeEventOffset, javaClassName);
        unsafe.putObject(beforeEvent, javaMethodNameFieldInBeforeEventOffset, javaMethodName);
        unsafe.putObject(beforeEvent, javaMethodDescFieldInBeforeEventOffset, javaMethodDesc);
        unsafe.putObject(beforeEvent, targetFieldInBeforeEventOffset, target);
        unsafe.putObject(beforeEvent, argumentArrayFieldInBeforeEventOffset, argumentArray);
        return beforeEvent;
    }

    public ReturnEvent makeReturnEvent(final int processId,
                                       final int invokeId,
                                       final Object returnObj) {
        if (null == returnEvent) {
            returnEvent = new ReturnEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, null);
        }
        unsafe.putInt(returnEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(returnEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putObject(returnEvent, objectFieldInReturnEventOffset, returnObj);
        return returnEvent;
    }

    public ImmediatelyReturnEvent makeImmediatelyReturnEvent(final int processId,
                                                             final int invokeId,
                                                             final Object returnObj) {
        if (null == immediatelyReturnEvent) {
            immediatelyReturnEvent = new ImmediatelyReturnEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, null);
        }
        unsafe.putInt(immediatelyReturnEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(immediatelyReturnEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putObject(immediatelyReturnEvent, objectFieldInReturnEventOffset, returnObj);
        return immediatelyReturnEvent;
    }

    public ThrowsEvent makeThrowsEvent(final int processId,
                                       final int invokeId,
                                       final Throwable throwable) {
        if (null == throwsEvent) {
            throwsEvent = new ThrowsEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, null);
        }
        unsafe.putInt(throwsEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(throwsEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putObject(throwsEvent, throwableFieldInThrowsEventOffset, throwable);
        return throwsEvent;
    }

    public ImmediatelyThrowsEvent makeImmediatelyThrowsEvent(final int processId,
                                                             final int invokeId,
                                                             final Throwable throwable) {
        if (null == immediatelyThrowsEvent) {
            immediatelyThrowsEvent = new ImmediatelyThrowsEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, null);
        }
        unsafe.putInt(immediatelyThrowsEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(immediatelyThrowsEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putObject(immediatelyThrowsEvent, throwableFieldInThrowsEventOffset, throwable);
        return immediatelyThrowsEvent;
    }


    public LineEvent makeLineEvent(final int processId,
                                   final int invokeId,
                                   final int lineNumber) {
        if (null == lineEvent) {
            lineEvent = new LineEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, -1);
        }
        unsafe.putInt(lineEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(lineEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putInt(lineEvent, lineNumberFieldInLineEventOffset, lineNumber);
        return lineEvent;
    }

    public CallBeforeEvent makeCallBeforeEvent(final int processId,
                                               final int invokeId,
                                               final int lineNumber,
                                               final String owner,
                                               final String name,
                                               final String desc) {
        if (null == callBeforeEvent) {
            callBeforeEvent = new CallBeforeEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, -1, null, null, null);
        }
        unsafe.putInt(callBeforeEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(callBeforeEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putInt(callBeforeEvent, lineNumberFieldInCallBeforeEventOffset, lineNumber);
        unsafe.putObject(callBeforeEvent, ownerFieldInCallBeforeEventOffset, owner);
        unsafe.putObject(callBeforeEvent, nameFieldInCallBeforeEventOffset, name);
        unsafe.putObject(callBeforeEvent, descFieldInCallBeforeEventOffset, desc);
        return callBeforeEvent;
    }

    public CallReturnEvent makeCallReturnEvent(final int processId,
                                               final int invokeId) {
        if (null == callReturnEvent) {
            callReturnEvent = new CallReturnEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID);
        }
        unsafe.putInt(callReturnEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(callReturnEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        return callReturnEvent;
    }

    public CallThrowsEvent makeCallThrowsEvent(final int processId,
                                               final int invokeId,
                                               final String throwException) {
        if (null == callThrowsEvent) {
            callThrowsEvent = new CallThrowsEvent(ILLEGAL_PROCESS_ID, ILLEGAL_INVOKE_ID, null);
        }
        unsafe.putInt(callThrowsEvent, processIdFieldInInvokeEventOffset, processId);
        unsafe.putInt(callThrowsEvent, invokeIdFieldInInvokeEventOffset, invokeId);
        unsafe.putObject(callThrowsEvent, throwExceptionFieldInCallThrowsEventOffset, throwException);
        return callThrowsEvent;
    }

    public void returnEvent(Event event) {
        switch (event.type) {
            case BEFORE:
                unsafe.putObject(event, targetFieldInBeforeEventOffset, null);
                unsafe.putObject(event, argumentArrayFieldInBeforeEventOffset, null);
                break;
            case IMMEDIATELY_THROWS:
            case THROWS:
                // FIXED #130
                // unsafe.putObject(event, throwableFieldInThrowsEventOffset, null);
                unCaughtSetClassDeclaredJavaFieldValue(ThrowsEvent.class, "throwable", event, null);
                break;
            case IMMEDIATELY_RETURN:
            case RETURN:
                // FIXED #130
                // unsafe.putObject(event, objectFieldInReturnEventOffset, null);
                unCaughtSetClassDeclaredJavaFieldValue(ReturnEvent.class, "object", event, null);
                break;
        }
    }

}
