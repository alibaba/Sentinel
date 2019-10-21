package com.alibaba.jvm.sandbox.api.http.websocket;

/**
 * WebSocket 连接监听器
 *
 * @author luanjia@taobao.com
 */
@Deprecated
public interface WebSocketConnectionListener {

    /**
     * 在WebSocketConnection创建成功后被调用
     *
     * @param conn WebSocketConnection
     *             可以使用这个连接完成和Client的通讯操作
     */
    void onOpen(WebSocketConnection conn);


    /**
     * 在WebSocketConnection被关闭后被调用
     *
     * @param closeCode WebSocket关闭码，描述了WebSocketConnection被关闭的原因
     *                  具体的错误码可以参考<a href="https://tools.ietf.org/html/rfc6455">RFC6455</a>
     * @param message   关闭信息
     */
    void onClose(int closeCode, String message);

}
