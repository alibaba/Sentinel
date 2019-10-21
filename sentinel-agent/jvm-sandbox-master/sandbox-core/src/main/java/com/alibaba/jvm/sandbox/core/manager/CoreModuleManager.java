package com.alibaba.jvm.sandbox.core.manager;

import com.alibaba.jvm.sandbox.api.ModuleException;
import com.alibaba.jvm.sandbox.core.CoreModule;

import java.util.Collection;

/**
 * 模块管理
 * Created by luanjia on 16/10/4.
 */
public interface CoreModuleManager {

    /**
     * 刷新沙箱模块
     *
     * @param isForce 是否强制刷新
     * @throws ModuleException 模块加载失败
     */
    void flush(boolean isForce) throws ModuleException;

    /**
     * 沙箱重置
     *
     * @return this
     * @throws ModuleException 沙箱重置失败
     */
    CoreModuleManager reset() throws ModuleException;

    /**
     * 激活模块
     *
     * @param coreModule 模块业务对象
     * @throws ModuleException 激活模块失败
     */
    void active(CoreModule coreModule) throws ModuleException;

    /**
     * 冻结模块
     * 模块冻结时候将会失去所有事件的监听
     *
     * @param coreModule              模块业务对象
     * @param isIgnoreModuleException 是否忽略模块异常
     *                                强制冻结模块将会主动忽略冻结失败情况，强行将模块所有的事件监听行为关闭
     * @throws ModuleException 冻结模块失败
     */
    void frozen(CoreModule coreModule, boolean isIgnoreModuleException) throws ModuleException;

    /**
     * 列出所有的模块
     *
     * @return 模块集合
     */
    Collection<CoreModule> list();

    /**
     * 获取模块
     *
     * @param uniqueId 模块ID
     * @return 模块
     */
    CoreModule get(String uniqueId);

    /**
     * 获取模块
     *
     * @param uniqueId 模块ID
     * @return 模块
     * @throws ModuleException 当模块不存在时抛出模块不存在异常
     */
    CoreModule getThrowsExceptionIfNull(String uniqueId) throws ModuleException;

    /**
     * 卸载模块
     *
     * @param coreModule              模块
     * @param isIgnoreModuleException 是否忽略模块异常
     * @return 返回被卸载的模块
     * @throws ModuleException 卸载模块失败
     */
    CoreModule unload(CoreModule coreModule, boolean isIgnoreModuleException) throws ModuleException;

    /**
     * 卸载所有模块
     */
    void unloadAll();

}
