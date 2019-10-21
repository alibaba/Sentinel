package com.alibaba.jvm.sandbox.core.manager.impl;

import com.alibaba.jvm.sandbox.api.ModuleException;
import com.alibaba.jvm.sandbox.api.resource.ModuleController;
import com.alibaba.jvm.sandbox.core.CoreModule;
import com.alibaba.jvm.sandbox.core.manager.CoreModuleManager;

/**
 * 沙箱模块控制器
 * Created by luanjia@taobao.com on 2016/11/14.
 */
class DefaultModuleController implements ModuleController {

    private final CoreModule coreModule;
    private final CoreModuleManager coreModuleManager;

    DefaultModuleController(final CoreModule coreModule,
                            final CoreModuleManager coreModuleManager) {
        this.coreModule = coreModule;
        this.coreModuleManager = coreModuleManager;
    }

    @Override
    public void active() throws ModuleException {
        coreModuleManager.active(coreModule);
    }

    @Override
    public void frozen() throws ModuleException {
        coreModuleManager.frozen(coreModule, false);
    }
}
