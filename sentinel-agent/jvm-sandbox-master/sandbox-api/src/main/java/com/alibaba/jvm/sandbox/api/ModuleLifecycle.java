package com.alibaba.jvm.sandbox.api;

/**
 * 沙箱模块生命周期
 *
 * @author luanjia@taobao.com
 */
public interface ModuleLifecycle extends LoadCompleted {

    /**
     * 模块加载，模块开始加载之前调用！
     * <p>
     * 模块加载是模块生命周期的开始，在模块生命中期中有且只会调用一次。
     * 这里抛出异常将会是阻止模块被加载的唯一方式，如果模块判定加载失败，将会释放掉所有预申请的资源，模块也不会被沙箱所感知
     * </p>
     *
     * @throws Throwable 加载模块失败
     */
    void onLoad() throws Throwable;


    /**
     * 模块卸载，模块开始卸载之前调用！
     * <p>
     * 模块卸载是模块生命周期的结束，在模块生命中期中有且只会调用一次。
     * 这里抛出异常将会是阻止模块被卸载的唯一方式，如果模块判定卸载失败，将不会造成任何资源的提前关闭与释放，模块将能继续正常工作
     * </p>
     *
     * @throws Throwable 卸载模块失败
     */
    void onUnload() throws Throwable;

    /**
     * 模块激活
     * <p>
     * 模块被激活后，模块所增强的类将会被激活，所有{@link com.alibaba.jvm.sandbox.api.listener.EventListener}将开始收到对应的事件
     * </p>
     * <p>
     * 这里抛出异常将会是阻止模块被激活的唯一方式
     * </p>
     *
     * @throws Throwable 模块激活失败
     */
    void onActive() throws Throwable;

    /**
     * 模块冻结
     * <p>
     * 模块被冻结后，模块所持有的所有{@link com.alibaba.jvm.sandbox.api.listener.EventListener}将被静默，无法收到对应的事件。
     * 需要注意的是，模块冻结后虽然不再收到相关事件，但沙箱给对应类织入的增强代码仍然还在。
     * </p>
     * <p>
     * 这里抛出异常将会是阻止模块被冻结的唯一方式
     * </p>
     *
     * @throws Throwable 模块冻结失败
     */
    void onFrozen() throws Throwable;

}
