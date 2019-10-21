package com.alibaba.jvm.sandbox.api.http.websocket;

import java.io.IOException;

/**
 * WebSocketConnection，用于描述一个WebSocket连接和封装对连接的操作
 *
 * @author luanjia@taobao.com
 */
@Deprecated
public interface WebSocketConnection {

    /**
     * 发送文本数据
     *
     * @param data 文本信息
     * @throws IOException 发送出错
     */
    void write(String data) throws IOException;

    /**
     * 断开websocket连接
     */
    void disconnect();

    /**
     * 当前websocket连接是否已经打开
     *
     * @return true:连接打开;false:连接关闭
     */
    boolean isOpen();

    /**
     * 设置最大闲置时间
     *
     * @param ms The time in ms that the connection can be idle before closing
     */
    void setMaxIdleTime(int ms);

}
