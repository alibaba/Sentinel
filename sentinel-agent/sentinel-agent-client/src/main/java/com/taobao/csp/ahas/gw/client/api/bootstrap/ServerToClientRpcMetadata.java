package com.taobao.csp.ahas.gw.client.api.bootstrap;

import com.taobao.csp.ahas.gw.client.api.exception.AgwRpcException;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.AgwStringUtil;

public class ServerToClientRpcMetadata extends BaseRpcMetadata<ServerToClientRpcMetadata> {
   private String clientVpcId;
   private String clientIp;
   private String clientProcessFlag;
   private String clientHandlerName;
   private int hsfTimeOut;
   public static final int MAX_RPC_TIMEOUT_S = 31;

   public String getClientVpcId() {
      return this.clientVpcId;
   }

   public ServerToClientRpcMetadata setClientVpcId(String clientVpcId) {
      this.clientVpcId = clientVpcId;
      return this;
   }

   public String getClientIp() {
      return this.clientIp;
   }

   public ServerToClientRpcMetadata setClientIp(String clientIp) {
      this.clientIp = clientIp;
      return this;
   }

   public String getClientProcessFlag() {
      return this.clientProcessFlag;
   }

   public ServerToClientRpcMetadata setClientProcessFlag(String clientProcessFlag) {
      this.clientProcessFlag = clientProcessFlag;
      return this;
   }

   public String getClientHandlerName() {
      return this.clientHandlerName;
   }

   public ServerToClientRpcMetadata setClientHandlerName(String clientHandlerName) {
      this.clientHandlerName = clientHandlerName;
      return this;
   }

   public int getHsfTimeOut() {
      return this.hsfTimeOut;
   }

   public ServerToClientRpcMetadata setHsfTimeOut(int hsfTimeOut) {
      this.hsfTimeOut = hsfTimeOut;
      return this;
   }

   public void check() {
      if (AgwStringUtil.isBlank(this.clientVpcId)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_VPCID_EMPTY_EXCEPTION);
      } else if (AgwStringUtil.isBlank(this.clientIp)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_IP_FORMAT_WRONG);
      } else if (AgwStringUtil.isBlank(this.clientProcessFlag)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_PROCESS_FLAG_EMPTY_EXCEPTION);
      } else if (AgwStringUtil.isBlank(this.clientHandlerName)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_HANDLER_NAME_NULL_EXCEPTION);
      }
   }
}
