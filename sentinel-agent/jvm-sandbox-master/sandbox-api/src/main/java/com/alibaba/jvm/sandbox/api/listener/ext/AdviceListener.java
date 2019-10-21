package com.alibaba.jvm.sandbox.api.listener.ext;

import com.alibaba.jvm.sandbox.api.ProcessController;
import com.alibaba.jvm.sandbox.api.event.Event;

/**
 * 通知监听器
 * <p>
 * 将{@link Event}转换为更友好的{@link Advice}，当然，代价是一定的性能开销
 * </p>
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public class AdviceListener {

    /**
     * 方法调用前通知
     * <ul>
     * <li>通知处理与当前业务同属一个线程</li>
     * <li>通知处理失败不会影响当前业务处理结果</li>
     * <li>
     * 在通知处理过程中依然可以使用{@link Advice#changeParameter(int, Object)}来改变入参，
     * 也可以继续使用{@link ProcessController}来改变代码执行流程
     * </li>
     * </ul>
     *
     * @param advice 通知信息
     * @throws Throwable 处理通知错误
     */
    protected void before(Advice advice) throws Throwable {
    }

    /**
     * 方法调用返回后通知
     * <ul>
     * <li>在这个通知环节，通过{@link Advice#getParameterArray()}拿到的入参有可能已经被方法内部改变过，不再是原有的入业务入参</li>
     * <li>
     * 在这个通知环节，你无法通过{@link Advice#changeParameter(int, Object)}来改变方法入参，
     * 因为整个方法已经执行过了，再次改变已经失去了意义
     * </li>
     * </ul>
     *
     * @param advice 通知信息
     * @throws Throwable 处理通知错误
     * @see #before(Advice)
     */
    protected void afterReturning(Advice advice) throws Throwable {

    }

    /**
     * 方法调用后通知，无论是正常返回还是抛出异常都会调用
     *
     * @param advice 通知信息
     * @throws Throwable 处理通知错误
     * @see #afterReturning(Advice)
     * @see #afterThrowing(Advice)
     */
    protected void after(Advice advice) throws Throwable {
    }

    /**
     * 方法调用抛出异常后通知
     *
     * @param advice 通知信息
     * @throws Throwable 处理通知错误
     * @see #afterReturning(Advice)
     */
    protected void afterThrowing(Advice advice) throws Throwable {

    }

    // --- 以下为CALL的调用处理 ---

    /**
     * 目标方法调用之前
     * <p>
     * 在一个方法调用过程中会调用其他的方法，CALL系列的事件就是来描述这一类调用的情况。
     * CALL系列事件必定是包含在BEFORE/RETURN/THROWS事件之间。
     * </p>
     *
     * @param advice             Caller的行为通知
     * @param callLineNum        调用发生的代码行(可能为-1，取决于目标编译代码的编译策略)
     * @param callJavaClassName  调用目标类名
     * @param callJavaMethodName 调用目标行为名称
     * @param callJavaMethodDesc 调用目标行为描述
     */
    protected void beforeCall(Advice advice,
                              int callLineNum,
                              String callJavaClassName, String callJavaMethodName, String callJavaMethodDesc) {

    }

    /**
     * 目标方法返回之后
     * <p>
     * 在一个方法调用过程中会调用其他的方法，CALL系列的事件就是来描述这一类调用的情况。
     * CALL系列事件必定是包含在BEFORE/RETURN/THROWS事件之间。
     * </p>
     *
     * @param advice             Caller的行为通知
     * @param callLineNum        调用发生的代码行(可能为-1，取决于目标编译代码的编译策略)
     * @param callJavaClassName  调用目标类名
     * @param callJavaMethodName 调用目标行为名称
     * @param callJavaMethodDesc 调用目标行为描述
     */
    protected void afterCallReturning(Advice advice,
                                      int callLineNum,
                                      String callJavaClassName, String callJavaMethodName, String callJavaMethodDesc) {

    }

    /**
     * 目标方法调用异常之后
     * <p>
     * 在一个方法调用过程中会调用其他的方法，CALL系列的事件就是来描述这一类调用的情况。
     * CALL系列事件必定是包含在BEFORE/RETURN/THROWS事件之间。
     * </p>
     *
     * @param advice                 Caller的行为通知
     * @param callLineNum            调用发生的代码行(可能为-1，取决于目标编译代码的编译策略)
     * @param callJavaClassName      调用目标类名
     * @param callJavaMethodName     调用目标行为名称
     * @param callJavaMethodDesc     调用目标行为描述
     * @param callThrowJavaClassName 调用目标异常类名
     */
    protected void afterCallThrowing(Advice advice,
                                     int callLineNum,
                                     String callJavaClassName, String callJavaMethodName, String callJavaMethodDesc,
                                     String callThrowJavaClassName) {

    }

    /**
     * 目标方法调用结束之后，无论正常返回还是抛出异常
     * <p>
     * 在一个方法调用过程中会调用其他的方法，CALL系列的事件就是来描述这一类调用的情况。
     * CALL系列事件必定是包含在BEFORE/RETURN/THROWS事件之间。
     * </p>
     *
     * @since {@code sandbox-api:1.2.2}
     *
     * @param advice                 Caller的行为通知
     * @param callLineNum            调用发生的代码行(可能为-1，取决于目标编译代码的编译策略)
     * @param callJavaClassName      调用目标类名
     * @param callJavaMethodName     调用目标行为名称
     * @param callJavaMethodDesc     调用目标行为描述
     * @param callThrowJavaClassName 调用目标异常类名，若正常返回则为 null
     */
    protected void afterCall(Advice advice,
                             int callLineNum,
                             String callJavaClassName,
                             String callJavaMethodName, String callJavaMethodDesc, String callThrowJavaClassName) {
    }

    /**
     * 行为即将经过的代码行
     *
     * @param advice  Caller的行为通知
     * @param lineNum 即将经过的代码行
     */
    protected void beforeLine(Advice advice, int lineNum) {

    }

}
