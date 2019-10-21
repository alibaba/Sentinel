package com.alibaba.jvm.sandbox.core;

import com.alibaba.jvm.sandbox.core.enhance.weaver.EventListenerHandlers;
import com.alibaba.jvm.sandbox.core.manager.CoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultCoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultLoadedClassDataSource;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultProviderManager;
import com.alibaba.jvm.sandbox.core.util.SpyUtils;

import java.lang.instrument.Instrumentation;

/**
 * 沙箱
 */
public class JvmSandbox {

    private final CoreConfigure cfg;
    private final CoreModuleManager coreModuleManager;

    public JvmSandbox(final CoreConfigure cfg,
                      final Instrumentation inst) {
        EventListenerHandlers.getSingleton();
        this.cfg = cfg;
        this.coreModuleManager = new DefaultCoreModuleManager(
                cfg,
                inst,
                new DefaultLoadedClassDataSource(inst, cfg.isEnableUnsafe()),
                new DefaultProviderManager(cfg)
        );

        init();
    }

    private void init() {
        SpyUtils.init(cfg.getNamespace());
    }


    /**
     * 获取模块管理器
     *
     * @return 模块管理器
     */
    public CoreModuleManager getCoreModuleManager() {
        return coreModuleManager;
    }

    /**
     * 销毁沙箱
     */
    public void destroy() {

        // 卸载所有的模块
        coreModuleManager.unloadAll();

        // 清理Spy
        SpyUtils.clean(cfg.getNamespace());

    }

}
