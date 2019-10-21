package com.alibaba.jvm.sandbox.api.http.websocket;

/**
 * 基于文本消息的WebSocket监听器
 *
 * @author luanjia@taobao.com
 */
@Deprecated
public interface TextMessageListener extends WebSocketConnectionListener {

    /**
     * 从客户端传送过来的文本字符串
     *
     * @param data 文本数据
     */
    void onMessage(String data);

}
