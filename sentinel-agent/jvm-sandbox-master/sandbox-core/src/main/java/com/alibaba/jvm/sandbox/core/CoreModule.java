package com.alibaba.jvm.sandbox.core;

import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.core.classloader.ModuleJarClassLoader;
import com.alibaba.jvm.sandbox.core.manager.impl.SandboxClassFileTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * 沙箱模块内核封装对象
 *
 * @author luanjia@taobao.com
 */
public class CoreModule {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 全局唯一编号
    private final String uniqueId;

    // 模块归属Jar文件
    private final File jarFile;

    // 模块加载的ClassLoader
    private final ModuleJarClassLoader loader;

    // 模块
    private final Module module;

    // 模块的类转换器
    private final Set<SandboxClassFileTransformer> sandboxClassFileTransformers
            = new LinkedHashSet<SandboxClassFileTransformer>();

    // 模块所持有的可释放资源
    private final List<ReleaseResource<?>> releaseResources
            = new ArrayList<ReleaseResource<?>>();

    // 是否已经激活
    private boolean isActivated;

    // 是否已被加载
    private boolean isLoaded;

    /**
     * 模块业务对象
     *
     * @param uniqueId 模块ID
     * @param jarFile  模块归属Jar文件
     * @param loader   模块加载ClassLoader
     * @param module   模块
     */
    public CoreModule(final String uniqueId,
                      final File jarFile,
                      final ModuleJarClassLoader loader,
                      final Module module) {
        this.uniqueId = uniqueId;
        this.jarFile = jarFile;
        this.loader = loader;
        this.module = module;
    }

    /**
     * 判断模块是否已被激活
     *
     * @return TRUE:已激活;FALSE:未激活
     */
    public boolean isActivated() {
        return isActivated;
    }

    /**
     * 标记模块激活状态
     *
     * @param isActivated 模块激活状态
     * @return this
     */
    public CoreModule markActivated(boolean isActivated) {
        this.isActivated = isActivated;
        return this;
    }

    /**
     * 判断模块是否已经被加载
     *
     * @return TRUE:被加载;FALSE:未被加载
     */
    public boolean isLoaded() {
        return isLoaded;
    }


    /**
     * 标记模块加载状态
     *
     * @param isLoaded 模块加载状态
     * @return this
     */
    public CoreModule markLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
        return this;
    }

    /**
     * 获取ModuleJar文件
     *
     * @return ModuleJar文件
     */
    public File getJarFile() {
        return jarFile;
    }

    /**
     * 获取对应的ModuleJarClassLoader
     *
     * @return ModuleJarClassLoader
     */
    public ModuleJarClassLoader getLoader() {
        return loader;
    }

    /**
     * 获取模块ID
     *
     * @return 模块ID
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * 获取模块实例
     *
     * @return 模块实例
     */
    public Module getModule() {
        return module;
    }

    /**
     * 获取模块所创建的SandboxClassFileTransformer集合
     *
     * @return 模块所创建的SandboxClassFileTransformer集合
     */
    public Set<SandboxClassFileTransformer> getSandboxClassFileTransformers() {
        return sandboxClassFileTransformers;
    }

    /**
     * 获取模块所编织的类个数
     *
     * @return 模块所编织的类个数
     */
    public int cCnt() {
        int cCnt = 0;
        for (final SandboxClassFileTransformer sandboxClassFileTransformer : sandboxClassFileTransformers) {
            cCnt += sandboxClassFileTransformer.getAffectStatistic().cCnt();
        }
        return cCnt;
    }

    /**
     * 获取模块所编织的方法个数
     *
     * @return 模块所编织的方法个数
     */
    public int mCnt() {
        int mCnt = 0;
        for (final SandboxClassFileTransformer sandboxClassFileTransformer : sandboxClassFileTransformers) {
            mCnt += sandboxClassFileTransformer.getAffectStatistic().mCnt();
        }
        return mCnt;
    }

    @Override
    public String toString() {
        return String.format(
                "module[id=%s;class=%s;]",
                uniqueId,
                module.getClass()
        );
    }


    /**
     * 在模块下追加一个可释放资源
     *
     * @param resource 可释放资源封装
     * @param <T>      资源实体
     * @return 资源实体本身
     */
    public <T> T append(ReleaseResource<T> resource) {
        if (null == resource
                || null == resource.get()) {
            return null;
        }
        releaseResources.add(resource);
        logger.debug("append resource={} in module[id={};]", resource.get(), uniqueId);
        return resource.get();
    }

    /**
     * 在当前模块下释放一个可释放资源
     *
     * @param target 待释放的资源实体
     */
    public void release(Object target) {
        final Iterator<ReleaseResource<?>> resourceRefIt = releaseResources.iterator();
        while (resourceRefIt.hasNext()) {
            final ReleaseResource<?> resourceRef = resourceRefIt.next();

            // 删除掉无效的资源
            if (null == resourceRef) {
                resourceRefIt.remove();
                logger.info("remove null resource in module={}", uniqueId);
                continue;
            }

            // 删除掉已经被GC掉的资源
            final Object resource = resourceRef.get();
            if (null == resource) {
                resourceRefIt.remove();
                logger.info("remove empty resource in module={}", uniqueId);
                continue;
            }

            if (target.equals(resource)) {
                resourceRefIt.remove();
                logger.debug("release resource={} in module={}", resourceRef.get(), uniqueId);
                try {
                    resourceRef.release();
                } catch (Exception cause) {
                    logger.warn("release resource occur error in module={};", uniqueId, cause);
                }
            }
        }
    }

    /**
     * 在当前模块下移除所有可释放资源
     */
    public void releaseAll() {
        final Iterator<ReleaseResource<?>> resourceRefIt = releaseResources.iterator();
        while (resourceRefIt.hasNext()) {
            final ReleaseResource<?> resourceRef = resourceRefIt.next();
            resourceRefIt.remove();
            if (null != resourceRef) {
                logger.debug("release resource={} in module={}", resourceRef.get(), uniqueId);
                try {
                    resourceRef.release();
                } catch (Exception cause) {
                    logger.warn("release resource occur error in module={};", uniqueId, cause);
                }
            }
        }
    }

    /**
     * 可释放资源
     *
     * @param <T> 资源类型
     */
    public static abstract class ReleaseResource<T> {

        // 资源弱引用，允许被GC回收
        private final WeakReference<T> reference;

        /**
         * 构造释放资源
         *
         * @param resource 资源目标
         */
        public ReleaseResource(T resource) {
            this.reference = new WeakReference<T>(resource);
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
            return reference.get();
        }

    }

}
