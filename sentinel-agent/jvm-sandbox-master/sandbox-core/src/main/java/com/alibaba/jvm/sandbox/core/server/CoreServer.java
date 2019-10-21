package com.alibaba.jvm.sandbox.core.server;

import com.alibaba.jvm.sandbox.core.CoreConfigure;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

/**
 * 内核服务器
 * Created by luanjia@taobao.com on 16/10/2.
 */
public interface CoreServer {

    /**
     * 判断服务器是否已经绑定端口
     *
     * @return 服务器是否已经绑定端口
     */
    boolean isBind();

    /**
     * 服务器解除端口绑定
     *
     * @throws IOException 解除绑定失败
     */
    void unbind() throws IOException;

    /**
     * 获取服务器绑定本地网络信息
     *
     * @return 服务器绑定本地网络信息
     * @throws IOException 绑定失败
     */
    InetSocketAddress getLocal() throws IOException;

    /**
     * 服务器绑定端口
     *
     * @param cfg  内核配置信息
     * @param inst inst
     * @throws IOException 绑定失败
     */
    void bind(CoreConfigure cfg, Instrumentation inst) throws IOException;

    /**
     * 销毁服务器
     */
    void destroy();

}
