package com.alibaba.jvm.sandbox.provider.api;

import java.io.File;

/**
 * 模块Jar文件加载链
 *
 * @author luanjia@taobao.com
 */
public interface ModuleJarLoadingChain {

    /**
     * 加载模块Jar文件 <br>
     * <p>
     * 1. 可以在这个实现中对目标期待加载的模块Jar文件进行解密,签名验证等操作<br>
     * 2. 如果判定加载失败,可以通过抛出异常的形式中断加载,sandbox将会跳过此模块Jar文件的加载<br>
     * 3. 整个模块文件的加载为一个链式的加载过程<br>
     * </p>
     *
     * @param moduleJarFile 期待被加载模块Jar文件
     * @throws Throwable 模块文件加载异常
     */
    void loading(File moduleJarFile) throws Throwable;

}
