package com.taobao.csp.ahas.gw.client.api.bootstrap;

import com.taobao.csp.ahas.gw.client.api.exception.AgwRpcException;
import com.taobao.csp.ahas.gw.client.api.handler.AgwHandler;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.AgwStringUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerCenter {
   private static final ConcurrentHashMap<String, AgwHandler> commandToHandlerMap = new ConcurrentHashMap();

   public static void addHandler(String handlerName, AgwHandler handler) {
      if (AgwStringUtil.isBlank(handlerName)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_HANDLER_NAME_NULL_EXCEPTION);
      } else if (null == handler) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_HANDLER_NULL_EXCEPTION);
      } else {
         AgwHandler old = (AgwHandler)commandToHandlerMap.putIfAbsent(handlerName, handler);
         if (old != null) {
            throw new IllegalStateException("duplicated command");
         }
      }
   }

   public static Set<String> getAllCommands() {
      return commandToHandlerMap.keySet();
   }

   public static AgwHandler getHandler(String handlerName) {
      return AgwStringUtil.isBlank(handlerName) ? null : (AgwHandler)commandToHandlerMap.get(handlerName);
   }
}
