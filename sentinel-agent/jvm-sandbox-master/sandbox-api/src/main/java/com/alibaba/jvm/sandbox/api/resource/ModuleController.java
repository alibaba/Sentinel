package com.alibaba.jvm.sandbox.api.resource;

import com.alibaba.jvm.sandbox.api.ModuleException;

/**
 * 模块控制接口
 *
 * @author luanjia@taobao.com
 */
public interface ModuleController {

    /**
     * 激活当前模块
     *
     * @throws ModuleException 激活模块失败
     */
    void active() throws ModuleException;

    /**
     * 冻结当前模块
     *
     * @throws ModuleException 冻结失败
     */
    void frozen() throws ModuleException;

}
