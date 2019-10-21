package com.taobao.csp.ahas.gw.processor.client;

import com.taobao.csp.ahas.gw.client.api.bootstrap.HandlerCenter;
import com.taobao.csp.ahas.gw.client.api.handler.AgwHandler;
import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.processor.IProcessor;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.thread.ThreadPoolManager;

public class BizRequestClientProcessor implements IProcessor {
   private static BizRequestClientProcessor instance = new BizRequestClientProcessor();

   private BizRequestClientProcessor() {
   }

   public static BizRequestClientProcessor getInstance() {
      return instance;
   }

   public void process(final Connection connection, final AgwMessage request) {
      ThreadPoolManager.getInstance().getClientThreadPool().execute(new Runnable() {
         public void run() {
            request.mark("gateway_call_client");
            AgwMessage response = BizRequestClientProcessor.this.callHandler(request);
            connection.writeAndFlush(response);
         }
      });
   }

   private AgwMessage callHandler(AgwMessage request) {
      String handlerName = request.getHeader().getHandlerName();
      AgwHandler handler = HandlerCenter.getHandler(handlerName);
      if (handler == null) {
         LoggerInit.LOGGER.warn(String.format("can not get handler by handlerName[%s], reqId:%d, outerReqId:%s", handlerName, request.getHeader().getReqId(), request.getHeader().getOuterReqId()));
         request.getHeader().setRpcResultCodeEnum(RpcResultCodeEnum.RPC_CLIENT_HANDLER_NOT_FOUND);
         request.getHeader().setMessageDirection((byte)2);
         return request;
      } else {
         String result = null;

         try {
            request.mark("before_handle");
            result = handler.handle(request.getBody());
            request.mark("after_handle");
         } catch (Throwable var6) {
            LoggerInit.LOGGER.warn(String.format("executor handler[%s] wrong, reqId:%d, outerReqId:%s", handlerName, request.getHeader().getReqId(), request.getHeader().getOuterReqId()), var6);
            request.mark("handle_exception");
            LoggerInit.LOGGER_PERF.info(request.getPerf());
            request.setBody("something wrong happen");
            request.getHeader().setRpcResultCodeEnum(RpcResultCodeEnum.RPC_CLIENT_HANDLER_EXECUTE_WRONG);
            request.getHeader().setMessageDirection((byte)2);
            return request;
         }

         if (result == null) {
            result = "";
         }

         request.setBody(result);
         request.getHeader().setRpcResultCodeEnum(RpcResultCodeEnum.RPC_OK);
         request.getHeader().setMessageDirection((byte)2);
         LoggerInit.LOGGER_PERF.info(request.getPerf());
         return request;
      }
   }
}
