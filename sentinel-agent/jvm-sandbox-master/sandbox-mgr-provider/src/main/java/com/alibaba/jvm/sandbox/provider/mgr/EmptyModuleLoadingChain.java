package com.alibaba.jvm.sandbox.provider.mgr;

import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.provider.api.ModuleLoadingChain;

import java.io.File;

/**
 * @author luanjia@taobao.com
 */
public class EmptyModuleLoadingChain implements ModuleLoadingChain {

    @Override
    public void loading(final String uniqueId,
                        final Class moduleClass,
                        final Module module,
                        final File moduleJarFile,
                        final ClassLoader moduleClassLoader) throws Throwable {

    }

}
