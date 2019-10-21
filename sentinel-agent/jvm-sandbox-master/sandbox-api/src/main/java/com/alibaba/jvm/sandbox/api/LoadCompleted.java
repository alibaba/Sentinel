package com.alibaba.jvm.sandbox.api;

/**
 * 模块加载完成回调
 * <p>
 * 因{@link #loadCompleted()}方法比较常用，所以单独出来成为一个接口，
 * 原有方法语意、触发时机保持不变
 * </p>
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public interface LoadCompleted {

    /**
     * 模块加载完成，模块完成加载后调用！
     * <p>
     * 模块完成加载是在模块完成所有资源加载、分配之后的回调，在模块生命中期中有且只会调用一次。
     * 这里抛出异常不会影响模块被加载成功的结果。
     * </p>
     * <p>
     * 模块加载完成之后，所有的基于模块的操作都可以在这个回调中进行
     * </p>
     */
    void loadCompleted();

}
