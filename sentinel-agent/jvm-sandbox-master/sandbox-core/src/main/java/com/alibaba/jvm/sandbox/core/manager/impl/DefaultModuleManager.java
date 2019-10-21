package com.alibaba.jvm.sandbox.core.manager.impl;

import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleException;
import com.alibaba.jvm.sandbox.api.resource.ModuleManager;
import com.alibaba.jvm.sandbox.core.CoreModule;
import com.alibaba.jvm.sandbox.core.manager.CoreModuleManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 默认ModuleManager实现
 * Created by luanjia@taobao.com on 2017/2/9.
 */
public class DefaultModuleManager implements ModuleManager {

    private final CoreModuleManager coreModuleManager;

    public DefaultModuleManager(CoreModuleManager coreModuleManager) {
        this.coreModuleManager = coreModuleManager;
    }

    @Override
    public void flush(boolean isForce) throws ModuleException {
        coreModuleManager.flush(isForce);
    }

    @Override
    public void reset() throws ModuleException {
        coreModuleManager.reset();
    }

    @Override
    public void unload(String uniqueId) throws ModuleException {
        final CoreModule coreModule = coreModuleManager.getThrowsExceptionIfNull(uniqueId);
        coreModuleManager.unload(coreModule, false);
    }

    @Override
    public void active(String uniqueId) throws ModuleException {
        final CoreModule coreModule = coreModuleManager.getThrowsExceptionIfNull(uniqueId);
        coreModuleManager.active(coreModule);
    }

    @Override
    public void frozen(String uniqueId) throws ModuleException {
        final CoreModule coreModule = coreModuleManager.getThrowsExceptionIfNull(uniqueId);
        coreModuleManager.frozen(coreModule, false);
    }

    @Override
    public Collection<Module> list() {
        final Collection<Module> modules = new ArrayList<Module>();
        for (final CoreModule coreModule : coreModuleManager.list()) {
            modules.add(coreModule.getModule());
        }
        return modules;
    }

    @Override
    public Module get(String uniqueId) {
        final CoreModule coreModule = coreModuleManager.get(uniqueId);
        return null == coreModule
                ? null
                : coreModule.getModule();
    }

    @Override
    public int cCnt(String uniqueId) throws ModuleException {
        return coreModuleManager.getThrowsExceptionIfNull(uniqueId).cCnt();
    }

    @Override
    public int mCnt(String uniqueId) throws ModuleException {
        return coreModuleManager.getThrowsExceptionIfNull(uniqueId).mCnt();
    }

    @Override
    public boolean isActivated(String uniqueId) throws ModuleException {
        return coreModuleManager.getThrowsExceptionIfNull(uniqueId).isActivated();
    }

    @Override
    public boolean isLoaded(String uniqueId) throws ModuleException {
        return coreModuleManager.getThrowsExceptionIfNull(uniqueId).isLoaded();
    }

    @Override
    public File getJarFile(String uniqueId) throws ModuleException {
        return coreModuleManager.getThrowsExceptionIfNull(uniqueId).getJarFile();
    }

}
