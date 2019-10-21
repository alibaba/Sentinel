package com.taobao.csp.ahas.gw.client.api.bootstrap;

import com.taobao.csp.ahas.gw.client.api.exception.AgwRpcException;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.AgwStringUtil;

public class ClientToServerRpcMetadata extends BaseRpcMetadata<ClientToServerRpcMetadata> {
   private String serverName;
   private String serverHandlerName;

   public String getServerName() {
      return this.serverName;
   }

   public ClientToServerRpcMetadata setServerName(String serverName) {
      this.serverName = serverName;
      return this;
   }

   public String getServerHandlerName() {
      return this.serverHandlerName;
   }

   public ClientToServerRpcMetadata setServerHandlerName(String serverHandlerName) {
      this.serverHandlerName = serverHandlerName;
      return this;
   }

   public void check() {
      if (AgwStringUtil.isBlank(this.serverName)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_SERVER_NAME_NULL_EXCEPTION);
      } else if (AgwStringUtil.isBlank(this.serverHandlerName)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_HANDLER_NAME_NULL_EXCEPTION);
      }
   }
}
