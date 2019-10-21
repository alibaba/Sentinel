package com.alibaba.jvm.sandbox.api.spi;

/**
 * 模块文件卸载
 *
 * @author oldmanpushcart@gmail.com
 * @since {@code sandbox-api:1.2.0}
 */
public interface ModuleJarUnLoadSpi {

    /**
     * 模块Jar文件卸载完所有模块后，正式卸载Jar文件之前调用！
     */
    void onJarUnLoadCompleted();

}
