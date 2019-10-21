package com.taobao.csp.ahas.gw.client.api.bootstrap;

import com.taobao.csp.ahas.gw.client.api.exception.AgwRpcException;
import com.taobao.csp.ahas.gw.client.api.handler.AgwHandler;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.AgwStringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseAgwClient<B extends BaseAgwConfig<B>, C extends BaseRpcMetadata<C>, D extends ResponseCallback> {
   protected final AtomicBoolean initFlag = new AtomicBoolean(false);
   private static final Map<String, String> initParamMap = new HashMap();

   public final boolean init(B config) {
      if (!this.initFlag.compareAndSet(false, true)) {
         return true;
      } else if (config == null) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CONFIG_NULL_EXCEPTION);
      } else {
         return this.init0(config);
      }
   }

   protected abstract boolean init0(B var1);

   public BaseAgwClient addHandler(String handlerName, AgwHandler handler) {
      HandlerCenter.addHandler(handlerName, handler);
      return this;
   }

   protected abstract String call0(String var1, C var2, String var3);

   public Set<String> getAllCommands() {
      return HandlerCenter.getAllCommands();
   }

   /** @deprecated */
   @Deprecated
   public final String call(C rpcMetadata, String jsonParam) {
      if (!this.initFlag.get()) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_UNINIT_EXCEPTION);
      } else {
         return this.call0(this.genReqId(), rpcMetadata, jsonParam);
      }
   }

   public final String call(String reqId, C rpcMetadata, String jsonParam) {
      if (!this.initFlag.get()) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_UNINIT_EXCEPTION);
      } else if (AgwStringUtil.isBlank(reqId)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_OUTER_REQ_ID_BLANK_EXCEPTION);
      } else {
         return this.call0(reqId, rpcMetadata, jsonParam);
      }
   }

   public final String callWithCallBack(C rpcMetadata, String jsonParam, D callback) {
      if (!this.initFlag.get()) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_UNINIT_EXCEPTION);
      } else {
         return this.callWithCallBack0(this.genReqId(), rpcMetadata, jsonParam, callback);
      }
   }

   private String genReqId() {
      return "noReqId-" + UUID.randomUUID().toString();
   }

   public final String callWithCallBack(String reqId, C rpcMetadata, String jsonParam, D callback) {
      if (!this.initFlag.get()) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_UNINIT_EXCEPTION);
      } else if (AgwStringUtil.isBlank(reqId)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_OUTER_REQ_ID_BLANK_EXCEPTION);
      } else {
         return this.callWithCallBack0(reqId, rpcMetadata, jsonParam, callback);
      }
   }

   protected abstract String callWithCallBack0(String var1, C var2, String var3, D var4);

   public void destroy() {
      if (this.initFlag.get()) {
         this.destroy0();
      }
   }

   protected abstract void destroy0();
}
