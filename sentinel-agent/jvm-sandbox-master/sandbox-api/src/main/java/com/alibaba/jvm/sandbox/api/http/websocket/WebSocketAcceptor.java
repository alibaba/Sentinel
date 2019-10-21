package com.alibaba.jvm.sandbox.api.http.websocket;

import javax.servlet.http.HttpServletRequest;

/**
 * WebSocketConnection监听器的构造工厂
 *
 * @author luanjia@taobao.com
 */
@Deprecated
public interface WebSocketAcceptor {

    /**
     * 接受WebSocket创建连接请求
     * 当从客户端发起一个WebSocket连接请求的时候会调用这个方法，从这个方法中返回一个连接处理的监听器来完成本次WebSocket请求
     *
     * @param req      http req
     * @param protocol websocket's protocol
     * @return WebSocketConnectionListener
     */
    WebSocketConnectionListener onAccept(HttpServletRequest req, String protocol);

}
