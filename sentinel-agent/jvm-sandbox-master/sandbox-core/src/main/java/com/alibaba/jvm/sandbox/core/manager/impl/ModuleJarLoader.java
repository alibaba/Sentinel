package com.alibaba.jvm.sandbox.core.manager.impl;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.core.classloader.ModuleJarClassLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

class ModuleJarLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 等待加载的模块jar文件
    private final File moduleJarFile;

    // 沙箱加载模式
    private final Information.Mode mode;

    ModuleJarLoader(final File moduleJarFile,
                    final Information.Mode mode) {
        this.moduleJarFile = moduleJarFile;
        this.mode = mode;
    }


    private boolean loadingModules(final ModuleJarClassLoader moduleClassLoader,
                                   final ModuleLoadCallback mCb) {

        final Set<String> loadedModuleUniqueIds = new LinkedHashSet<String>();
        final ServiceLoader<Module> moduleServiceLoader = ServiceLoader.load(Module.class, moduleClassLoader);
        final Iterator<Module> moduleIt = moduleServiceLoader.iterator();
        while (moduleIt.hasNext()) {

            final Module module;
            try {
                module = moduleIt.next();
            } catch (Throwable cause) {
                logger.warn("loading module instance failed: instance occur error, will be ignored. module-jar={}", moduleJarFile, cause);
                continue;
            }

            final Class<?> classOfModule = module.getClass();

            // 判断模块是否实现了@Information标记
            if (!classOfModule.isAnnotationPresent(Information.class)) {
                logger.warn("loading module instance failed: not implements @Information, will be ignored. class={};module-jar={};",
                        classOfModule,
                        moduleJarFile
                );
                continue;
            }

            final Information info = classOfModule.getAnnotation(Information.class);
            final String uniqueId = info.id();

            // 判断模块ID是否合法
            if (StringUtils.isBlank(uniqueId)) {
                logger.warn("loading module instance failed: @Information.id is missing, will be ignored. class={};module-jar={};",
                        classOfModule,
                        moduleJarFile
                );
                continue;
            }

            // 判断模块要求的启动模式和容器的启动模式是否匹配
            if (!ArrayUtils.contains(info.mode(), mode)) {
                logger.warn("loading module instance failed: launch-mode is not match module required, will be ignored. module={};launch-mode={};required-mode={};class={};module-jar={};",
                        uniqueId,
                        mode,
                        StringUtils.join(info.mode(), ","),
                        classOfModule,
                        moduleJarFile
                );
                continue;
            }

            try {
                if (null != mCb) {
                    mCb.onLoad(uniqueId, classOfModule, module, moduleJarFile, moduleClassLoader);
                }
            } catch (Throwable cause) {
                logger.warn("loading module instance failed: MODULE-LOADER-PROVIDER denied, will be ignored. module={};class={};module-jar={};",
                        uniqueId,
                        classOfModule,
                        moduleJarFile,
                        cause
                );
                continue;
            }

            loadedModuleUniqueIds.add(uniqueId);

        }


        logger.info("loaded module-jar completed, loaded {} module in module-jar={}, modules={}",
                loadedModuleUniqueIds.size(),
                moduleJarFile,
                loadedModuleUniqueIds
        );
        return !loadedModuleUniqueIds.isEmpty();
    }


    void load(final ModuleLoadCallback mCb) throws IOException {

        boolean hasModuleLoadedSuccessFlag = false;
        ModuleJarClassLoader moduleJarClassLoader = null;
        logger.info("prepare loading module-jar={};", moduleJarFile);
        try {
            moduleJarClassLoader = new ModuleJarClassLoader(moduleJarFile);

            final ClassLoader preTCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(moduleJarClassLoader);

            try {
                hasModuleLoadedSuccessFlag = loadingModules(moduleJarClassLoader, mCb);
            } finally {
                Thread.currentThread().setContextClassLoader(preTCL);
            }

        } finally {
            if (!hasModuleLoadedSuccessFlag
                    && null != moduleJarClassLoader) {
                logger.warn("loading module-jar completed, but NONE module loaded, will be close ModuleJarClassLoader. module-jar={};", moduleJarFile);
                moduleJarClassLoader.closeIfPossible();
            }
        }

    }

    /**
     * 模块加载回调
     */
    public interface ModuleLoadCallback {

        /**
         * 模块加载回调
         *
         * @param uniqueId          模块ID
         * @param moduleClass       模块类
         * @param module            模块实例
         * @param moduleJarFile     模块所在Jar文件
         * @param moduleClassLoader 负责加载模块的ClassLoader
         * @throws Throwable 加载回调异常
         */
        void onLoad(String uniqueId,
                    Class moduleClass,
                    Module module,
                    File moduleJarFile,
                    ModuleJarClassLoader moduleClassLoader) throws Throwable;

    }

}
