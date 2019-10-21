package com.alibaba.jvm.sandbox.api.resource;

import com.alibaba.jvm.sandbox.api.Information;

import java.net.InetSocketAddress;

/**
 * 沙箱配置信息
 *
 * @author luanjia@taobao.com
 */
public interface ConfigInfo {

    /**
     * 获取沙箱的命名空间
     *
     * @return 沙箱的命名空间
     * @since {@code sandbox-common-api:1.0.2}
     */
    String getNamespace();

    /**
     * 获取沙箱的加载模式
     *
     * @return 沙箱加载模式
     */
    Information.Mode getMode();

    /**
     * 判断沙箱是否启用了unsafe
     * <p>unsafe功能启用之后，沙箱将能修改被BootstrapClassLoader所加载的类</p>
     * <p>在<b>${SANDBOX_HOME}/cfg/sandbox.properties#unsafe.enable</b>中进行开启关闭</p>
     *
     * @return true:功能启用;false:功能未启用
     */
    boolean isEnableUnsafe();

    /**
     * 获取沙箱的HOME目录(沙箱主程序目录)
     * 默认是在<b>${HOME}/.sandbox</b>
     *
     * @return 沙箱HOME目录
     */
    String getHome();

    /**
     * 获取沙箱的系统模块目录地址
     *
     * @return 系统模块目录地址
     * @deprecated 已经废弃, 可以参考{@link #getSystemModuleLibPath()}
     */
    @Deprecated
    String getModuleLibPath();

    /**
     * 获取沙箱的系统模块目录地址
     * <p>沙箱将会从该模块目录中寻找并加载所有的模块</p>
     * <p>默认路径在<b>${SANDBOX_HOME}/module</b>目录下</p>
     *
     * @return 系统模块目录地址
     */
    String getSystemModuleLibPath();

    /**
     * 获取沙箱内部服务提供库目录
     *
     * @return 沙箱内部服务提供库目录
     */
    String getSystemProviderLibPath();

    /**
     * 获取沙箱的用户模块目录地址
     * <p>沙箱将会优先从系统模块地址{@link #getModuleLibPath()}加载模块，然后再从用户模块目录地址加载模块</p>
     * <p>固定在<b>${HOME}/.sandbox-module</b>目录下</p>
     *
     * @return 用户模块目录地址
     * @deprecated 已经废弃，因为用户地址允许配置多条，可以通过{@link #getUserModuleLibPaths()}来获取所有的用户模块地址
     */
    @Deprecated
    String getUserModuleLibPath();

    /**
     * 获取沙箱的用户模块目录地址(集合)
     *
     * @return 用户模块目录地址(集合)
     */
    String[] getUserModuleLibPaths();

    /**
     * 判断沙箱是否启用了事件对象池
     * <p>启用事件对象池之后将会极大降低沙箱对JVM新生代的压力，但同时会带来一定的调用开销</p>
     * <p>是否启用对象池需要根据当前拦截的事件频繁程度来判断</p>
     * <p>在<b>${SANDBOX_HOME}/cfg/sandbox.properties#event.pool.enable</b>中进行开启关闭</p>
     *
     * @return true:启用;false:不启用
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    boolean isEnableEventPool();

    /**
     * 沙箱事件对象池单个事件类型缓存最小数量，{@link #isEnableEventPool()}==true时候有意义
     *
     * @return 单个事件类型缓存最小数量
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    int getEventPoolKeyMin();

    /**
     * 沙箱事件对象池单个事件类型缓存最大数量，{@link #isEnableEventPool()}==true时候有意义
     *
     * @return 单个事件类型缓存最大数量
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    int getEventPoolKeyMax();

    /**
     * 沙箱事件对象池所有事件类型缓存最大总数量，{@link #isEnableEventPool()}==true时候有意义
     *
     * @return 所有事件类型缓存最大总数量
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    int getEventPoolTotal();

    /**
     * 获取事件池最大容量
     *
     * @return 事件池最大容量
     * @since {@code sandbox-common-api:1.0.1}
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    int getEventPoolMaxTotal();

    /**
     * 获取事件池每个事件最小空闲容量
     *
     * @return 事件池每个事件最小空闲容量
     * @since {@code sandbox-common-api:1.0.1}
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    int getEventPoolMinIdlePerEvent();

    /**
     * 获取事件池每个事件最大空闲容量
     *
     * @return 事件池每个事件最大空闲容量
     * @since {@code sandbox-common-api:1.0.1}
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    int getEventPoolMaxIdlePerEvent();

    /**
     * 获取事件池每个事件最大容量
     *
     * @return 事件池每个事件最大容量
     * @since {@code sandbox-common-api:1.0.1}
     * @deprecated 后续不再支持事件池
     */
    @Deprecated
    int getEventPoolMaxTotalPerEvent();


    /**
     * 获取沙箱HTTP服务侦听地址
     * 如果服务器未能完成端口的绑定，则返回("0.0.0.0:0")
     *
     * @return 沙箱HTTP服务侦听地址
     */
    InetSocketAddress getServerAddress();

    /**
     * 获取沙箱HTTP服务返回编码
     *
     * @return 沙箱HTTP服务返回编码
     * @since 1.2.2
     */
    String getServerCharset();

    /**
     * 获取沙箱版本号
     *
     * @return 沙箱版本号
     */
    String getVersion();

}
