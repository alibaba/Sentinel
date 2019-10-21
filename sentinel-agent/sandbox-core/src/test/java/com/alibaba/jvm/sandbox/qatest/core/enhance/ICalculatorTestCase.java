package com.alibaba.jvm.sandbox.qatest.core.enhance;

/**
 * Calculator类测试用例接口
 * <p>
 * 用例命名规范：{@code <类名>$<方法名>[_方法名]$<执行时机>$<执行动作>[_执行动作]}
 * <p>
 * 例子：{@code cal$sum$before$changeParameters} : cal.sum()方法执行之前修改参数
 */
public interface ICalculatorTestCase {

    // ------ sum() -------

    /**
     * cal.sum()：环绕
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$around() throws Throwable;

    /**
     * cal.sum()：行跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$line() throws Throwable;

    /**
     * cal.sum()：方法调用跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$call() throws Throwable;

    /**
     * cal.sum()：方法执行之前修改参数
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$before$changeParameters() throws Throwable;

    /**
     * cal.sum()：方法执行之前立即返回
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$before$returnImmediately() throws Throwable;

    /**
     * cal.sum()：方法执行之前立即抛出异常
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$before$throwsImmediately() throws Throwable;

    /**
     * cal.sum()：返回之前修改参数
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$return$changeParameters() throws Throwable;

    /**
     * cal.sum()：返回之前修改返回值
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$return$returnImmediately() throws Throwable;

    /**
     * cal.sum()：返回之前抛出异常
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$return$throwsImmediately() throws Throwable;

    /**
     * cal.sum()：抛出异常之前修改入参
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$throws$changeParameters() throws Throwable;

    /**
     * cal.sum()：抛出异常之前立即返回
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$throws$returnImmediately() throws Throwable;

    /**
     * cal.sum()：抛出异常之前立即抛出异常
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum$throws$throwsImmediately() throws Throwable;


    // ------- sum()_add() --------

    /**
     * cal.sum()_add()：环绕
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$around() throws Throwable;

    /**
     * cal.sum()_add()：行跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$line() throws Throwable;

    /**
     * cal.sum()_add()：方法调用跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$call() throws Throwable;

    /**
     * cal.sum()_add()：方法执行之前修改参数
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$before$changeParameters_at_add() throws Throwable;

    /**
     * cal.sum()_add()：方法执行之前立即返回
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$before$returnImmediately_at_add() throws Throwable;

    /**
     * sum()_add()：方法执行之前立即抛出异常
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$before$throwsImmediately_at_add() throws Throwable;

    /**
     * cal.sum()_add()：返回之前修改参数
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$return$changeParameters_at_add() throws Throwable;

    /**
     * cal.sum()_add()：返回之前修改返回值
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$return$returnImmediately_at_add() throws Throwable;

    /**
     * cal.sum()_add()：返回之前抛出异常
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$return$throwsImmediately_at_add() throws Throwable;

    /**
     * cal.sum()_add()：抛出异常之前修改入参
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$throws$changeParameters_at_add() throws Throwable;

    /**
     * cal.sum()_add()：抛出异常之前立即返回
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$throws$returnImmediately_at_add() throws Throwable;

    /**
     * cal.sum()_add()：抛出异常之前立即抛出异常
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$sum_add$throws$throwsImmediately_at_add() throws Throwable;


    // ------- pow() -------

    /**
     * cal.pow()：环绕
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$pow$around() throws Throwable;

    /**
     * cal.pow()：行跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$pow$line() throws Throwable;

    /**
     * cal.pow()：方法调用跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$pow$call() throws Throwable;


    // ------- <init>() --------

    /**
     * {@code <init>(TestCase)}：环绕
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$init_with_TestCase$around() throws Throwable;

    /**
     * {@code <init>(TestCase)}：行跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$init_with_TestCase$line() throws Throwable;

    /**
     * {@code <init>(TestCase)}：调用跟踪
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$init_with_TestCase$call() throws Throwable;

    /**
     * {@code <init>(TestCase)}：改变入参
     *
     * @throws Throwable 用例抛出异常
     */
    void cal$init_with_TestCase$before$changeParameters() throws Throwable;

}
