package com.alibaba.jvm.sandbox.core.manager.impl;

import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.resource.ConfigInfo;
import com.alibaba.jvm.sandbox.core.CoreConfigure;
import com.alibaba.jvm.sandbox.core.classloader.ProviderClassLoader;
import com.alibaba.jvm.sandbox.core.manager.ProviderManager;
import com.alibaba.jvm.sandbox.provider.api.ModuleJarLoadingChain;
import com.alibaba.jvm.sandbox.provider.api.ModuleLoadingChain;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

/**
 * 默认服务提供管理器实现
 *
 * @author luanjia@taobao.com
 */
public class DefaultProviderManager implements ProviderManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Collection<ModuleJarLoadingChain> moduleJarLoadingChains = new ArrayList<ModuleJarLoadingChain>();
    private final Collection<ModuleLoadingChain> moduleLoadingChains = new ArrayList<ModuleLoadingChain>();
    private final CoreConfigure cfg;

    public DefaultProviderManager(final CoreConfigure cfg) {
        this.cfg = cfg;
        try {
            init(cfg);
        } catch (Throwable cause) {
            logger.warn("loading sandbox's provider-lib[{}] failed.", cfg.getProviderLibPath(), cause);
        }
    }

    private void init(final CoreConfigure cfg) {
        final File providerLibDir = new File(cfg.getProviderLibPath());
        if (!providerLibDir.exists()
                || !providerLibDir.canRead()) {
            logger.warn("loading provider-lib[{}] was failed, doest existed or access denied.", providerLibDir);
            return;
        }

        for (final File providerJarFile : FileUtils.listFiles(providerLibDir, new String[]{"jar"}, false)) {

            try {
                final ProviderClassLoader providerClassLoader = new ProviderClassLoader(providerJarFile, getClass().getClassLoader());

                // load ModuleJarLoadingChain
                inject(moduleJarLoadingChains, ModuleJarLoadingChain.class, providerClassLoader, providerJarFile);

                // load ModuleLoadingChain
                inject(moduleLoadingChains, ModuleLoadingChain.class, providerClassLoader, providerJarFile);

                logger.info("loading provider-jar[{}] was success.", providerJarFile);
            } catch (IllegalAccessException cause) {
                logger.warn("loading provider-jar[{}] occur error, inject provider resource failed.", providerJarFile, cause);
            } catch (IOException ioe) {
                logger.warn("loading provider-jar[{}] occur error, ignore load this provider.", providerJarFile, ioe);
            }

        }

    }

    private <T> void inject(final Collection<T> collection,
                            final Class<T> clazz,
                            final ClassLoader providerClassLoader,
                            final File providerJarFile) throws IllegalAccessException {
        final ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz, providerClassLoader);
        for (final T provider : serviceLoader) {
            injectResource(provider);
            collection.add(provider);
            logger.info("loading provider[{}] was success from provider-jar[{}], impl={}",
                    clazz.getName(), providerJarFile, provider.getClass().getName());
        }
    }

    private void injectResource(final Object provider) throws IllegalAccessException {
        final Field[] resourceFieldArray = FieldUtils.getFieldsWithAnnotation(provider.getClass(), Resource.class);
        if (ArrayUtils.isEmpty(resourceFieldArray)) {
            return;
        }
        for (final Field resourceField : resourceFieldArray) {
            final Class<?> fieldType = resourceField.getType();
            // ConfigInfo注入
            if (ConfigInfo.class.isAssignableFrom(fieldType)) {
                final ConfigInfo configInfo = new DefaultConfigInfo(cfg);
                FieldUtils.writeField(resourceField, provider, configInfo, true);
            }
        }
    }

    @Override
    public void loading(final File moduleJarFile) throws Throwable {
        for (final ModuleJarLoadingChain chain : moduleJarLoadingChains) {
            chain.loading(moduleJarFile);
        }
    }

    @Override
    public void loading(final String uniqueId,
                        final Class moduleClass,
                        final Module module,
                        final File moduleJarFile,
                        final ClassLoader moduleClassLoader) throws Throwable {
        for (final ModuleLoadingChain chain : moduleLoadingChains) {
            chain.loading(
                    uniqueId,
                    moduleClass,
                    module,
                    moduleJarFile,
                    moduleClassLoader
            );
        }
    }
}
