package com.taobao.csp.ahas.gw.client.api.exception;

import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;

public class AgwRpcException extends RuntimeException {
   public AgwRpcException() {
   }

   public AgwRpcException(String message) {
      super(message);
   }

   public AgwRpcException(int code, String message) {
      super(String.format("agw rpc exception [code: %d, message: %s]", code, message));
   }

   public AgwRpcException(RpcResultCodeEnum rpcException) {
      this(rpcException.getCode(), rpcException.getMessage());
   }

   public AgwRpcException(String message, Throwable cause) {
      super(message, cause);
   }

   public AgwRpcException(Throwable cause) {
      super(cause);
   }
}
