package com.alibaba.jvm.sandbox.core.manager;

import com.alibaba.jvm.sandbox.core.manager.ModuleLifeCycleEventBus.ModuleLifeCycleEventListener;

import java.lang.ref.WeakReference;

/**
 * 模块可释放资源管理
 * 通常用来管理围绕Module所展开的http/websocket连接
 * Created by luanjia@taobao.com on 2017/2/4.
 */
public interface ModuleResourceManager extends ModuleLifeCycleEventListener {

    /**
     * 在模块下追加一个可释放资源
     *
     * @param uniqueId 模块ID
     * @param resource 可释放资源封装
     * @param <T>      资源实体
     * @return 资源实体本身
     */
    <T> T append(String uniqueId, WeakResource<T> resource);

    /**
     * 在当前模块下移除一个可释放资源
     *
     * @param uniqueId 模块ID
     * @param target   待释放的资源实体
     * @param <T>      资源实体
     */
    <T> void remove(String uniqueId, T target);

    /**
     * 弱引用资源
     *
     * @param <T> 资源类型
     */
    abstract class WeakResource<T> {

        private final WeakReference<T> weakReference;

        public WeakResource(T resource) {
            this.weakReference = new WeakReference<T>(resource);
        }

        /**
         * 释放资源
         */
        public abstract void release();

        /**
         * 获取资源实体
         *
         * @return 资源实体
         */
        public T get() {
            return weakReference.get();
        }

    }

}