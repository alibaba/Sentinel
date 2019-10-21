package com.alibaba.jvm.sandbox.core.domain;

import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.core.classloader.ModuleClassLoader;
import com.alibaba.jvm.sandbox.core.manager.impl.SandboxClassFileTransformer;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 沙箱模块内核封装对象
 * Created by luanjia on 16/10/4.
 */
public class CoreModule {

    // 全局唯一编号
    private final String uniqueId;

    // 模块归属Jar文件
    private final File jarFile;

    // 模块加载的ClassLoader
    private final ModuleClassLoader loader;

    // 模块
    private final Module module;

    // 模块的类转换器
    private final Set<SandboxClassFileTransformer> sandboxClassFileTransformers
            = new LinkedHashSet<SandboxClassFileTransformer>();

    // 是否已经激活
    private boolean activated;

    // 是否已被加载
    private boolean loaded;

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
                      final ModuleClassLoader loader,
                      final Module module) {
        this.uniqueId = uniqueId;
        this.jarFile = jarFile;
        this.loader = loader;
        this.module = module;
    }

    public boolean isActivated() {
        return activated;
    }

    public CoreModule setActivated(boolean activated) {
        this.activated = activated;
        return this;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public CoreModule setLoaded(boolean loaded) {
        this.loaded = loaded;
        return this;
    }

    public File getJarFile() {
        return jarFile;
    }

    public ModuleClassLoader getLoader() {
        return loader;
    }

    public Module getModule() {
        return module;
    }

    public Set<SandboxClassFileTransformer> getSandboxClassFileTransformers() {
        return sandboxClassFileTransformers;
    }

    public String getUniqueId() {
        return uniqueId;
    }

//    public int cCnt() {
//        int cCnt = 0;
//        for (final SandboxClassFileTransformer sandboxClassFileTransformer : sandboxClassFileTransformers) {
//            cCnt += sandboxClassFileTransformer.cCnt();
//        }
//        return cCnt;
//    }
//
//    public int mCnt() {
//        int mCnt = 0;
//        for (final SandboxClassFileTransformer sandboxClassFileTransformer : sandboxClassFileTransformers) {
//            mCnt += sandboxClassFileTransformer.mCnt();
//        }
//        return mCnt;
//    }

}