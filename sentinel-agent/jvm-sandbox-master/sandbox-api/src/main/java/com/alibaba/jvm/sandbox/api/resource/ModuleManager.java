package com.alibaba.jvm.sandbox.api.resource;

import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleException;

import java.io.File;
import java.util.Collection;

/**
 * 模块管理器
 *
 * @author luanjia@taobao.com
 */
public interface ModuleManager {

    /**
     * 刷新沙箱模块
     * <p>
     * 刷新沙箱模块将会对当前已经加载的用户模块进行卸载并再次重新加载,
     * 通常用于模块JAR文件更新之后对模块进行再次的重新加载
     * </p>
     *
     * @param isForce 是否强制刷新
     *                <p>强制刷新将会强制卸载所有已经加载的用户模块，并重新加载当前用户模块目录下所有的模块</p>
     *                <p>
     *                普通刷新将会寻找变动的用户模块文件，对已经加载但模块文件已经变动（删除or变更）的模块进行卸载。
     *                并重新加载当前用户模块目录中新增的模块
     *                </p>
     * @throws ModuleException 刷新模块失败
     */
    void flush(boolean isForce) throws ModuleException;

    /**
     * 沙箱模块重置
     * 沙箱模块重置时会强制冻结和强制卸载当前所有模块，并对系统模块和用户模块进行重新加载;
     * 加载过程中如果发生模块加载失败，则主动忽略掉加载失败的模块
     *
     * @throws ModuleException 模块重启失败
     */
    void reset() throws ModuleException;

    /**
     * 卸载指定模块
     *
     * @param uniqueId 模块ID
     * @throws ModuleException 卸载模块失败
     */
    void unload(String uniqueId) throws ModuleException;

    /**
     * 激活模块
     * 激活的模块能正常的感知Event
     *
     * @param uniqueId 模块ID
     * @throws ModuleException 激活模块失败
     */
    void active(String uniqueId) throws ModuleException;

    /**
     * 冻结模块
     * 冻结的模块将不会感知到Event,但代码的插桩还在
     *
     * @param uniqueId 模块ID
     * @throws ModuleException 冻结模块失败
     */
    void frozen(String uniqueId) throws ModuleException;

    /**
     * 列出所有的模块
     *
     * @return 模块集合
     */
    Collection<Module> list();

    /**
     * 获取模块
     *
     * @param uniqueId 模块ID
     * @return 模块
     */
    Module get(String uniqueId);

    /**
     * 获取模块当前渲染的类个数
     *
     * @param uniqueId 模块ID
     * @return 模块当前渲染的类个数
     * @throws ModuleException 模块不存在
     */
    int cCnt(String uniqueId) throws ModuleException;

    /**
     * 获取模块当前渲染的方法个数
     *
     * @param uniqueId 模块ID
     * @return 模块当前渲染的方法个数
     * @throws ModuleException 模块不存在
     */
    int mCnt(String uniqueId) throws ModuleException;

    /**
     * 获取模块激活状态，判断当前模块是否已经被激活
     *
     * @param uniqueId 模块ID
     * @return true:已激活;false:未激活
     * @throws ModuleException 模块不存在
     */
    boolean isActivated(String uniqueId) throws ModuleException;

    /**
     * 获取模块加载状态，判断当前模块是否已经被加载
     *
     * @param uniqueId 模块ID
     * @return true:已加载;false:未加载
     * @throws ModuleException 模块不存在
     */
    boolean isLoaded(String uniqueId) throws ModuleException;

    /**
     * 获取模块所在的Jar文件
     *
     * @param uniqueId 模块ID
     * @return 模块所在的Jar文件
     * @throws ModuleException 模块不存在
     */
    File getJarFile(String uniqueId) throws ModuleException;

}
