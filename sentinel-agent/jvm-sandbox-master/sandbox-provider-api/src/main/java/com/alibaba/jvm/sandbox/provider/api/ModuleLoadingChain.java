package com.alibaba.jvm.sandbox.provider.api;

import com.alibaba.jvm.sandbox.api.Module;

import java.io.File;

/**
 * 模块加载链
 *
 * @author luanjia@taobao.com
 */
public interface ModuleLoadingChain {

    /**
     * 加载模块<br>
     * <p>
     * 1. 所有模块都将会通过此方法完成模块的加载<br>
     * 2. 如果判定当前模块加载不通过,可以通过抛出异常的形式来通知当前模块加载失败,sandbox将会跳过加载失败的模块<br>
     * 3. 整个模块的加载为一个链式的加载过程<br>
     * </p>
     *
     * @param uniqueId          模块ID
     * @param moduleClass       模块类
     * @param module            模块实例
     * @param moduleJarFile     模块所在Jar文件
     * @param moduleClassLoader 负责加载模块的ClassLoader
     * @throws Throwable 模块加载异常
     */
    void loading(final String uniqueId, final Class moduleClass, final Module module, final File moduleJarFile,
                 final ClassLoader moduleClassLoader) throws Throwable;

}
