package com.taobao.csp.ahas.gw.client.bootstrap.outer;

import com.taobao.csp.ahas.gw.client.api.bootstrap.BaseAgwClient;
import com.taobao.csp.ahas.gw.client.api.bootstrap.ClientToServerRpcMetadata;
import com.taobao.csp.ahas.gw.client.api.bootstrap.ResponseCallback;
import com.taobao.csp.ahas.gw.client.api.exception.AgwRpcException;
import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.connection.client.ClientConnectionPool;
import com.taobao.csp.ahas.gw.io.client.ClientConfig;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageFactory;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import com.taobao.csp.ahas.gw.io.protocol.ReqBizMessageBuilder;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.oss.OssCert;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.AgwStringUtil;
import com.taobao.csp.ahas.gw.utils.misc.IpUtil;

public final class AgwClient extends BaseAgwClient<ClientToServerAgwConfig, ClientToServerRpcMetadata, ResponseCallback> {
   private static AgwClient client = new AgwClient();
   private String clientVpcId;
   private String clientIp;
   private String clientProcessFlag;
   private long clientIpLong;

   private AgwClient() {
   }

   public static AgwClient getInstance() {
      return client;
   }

   protected boolean init0(ClientToServerAgwConfig config) {
      String clientVpcId = config.getClientVpcId();
      String clientIp = config.getClientIp();
      String clientProcessFlag = config.getClientProcessFlag();
      if (AgwStringUtil.isBlank(clientVpcId)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_VPCID_EMPTY_EXCEPTION);
      } else if (AgwStringUtil.isBlank(clientIp)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_IP_FORMAT_WRONG);
      } else if (AgwStringUtil.isBlank(clientProcessFlag)) {
         throw new AgwRpcException(RpcResultCodeEnum.RPC_CLIENT_PROCESS_FLAG_EMPTY_EXCEPTION);
      } else {
         this.clientVpcId = clientVpcId;
         this.clientIp = clientIp;
         this.clientProcessFlag = clientProcessFlag;
         this.clientIpLong = IpUtil.ipToLong(clientIp);
         ClientConfig.init(config.getAhasGatewayIp(), config.getAhasGatewayPort(), config.isTls(), config.getRegionId(), config.getEnv());
         if (ClientConfig.TLS) {
            OssCert.createOrUpdateClientCert(ClientConfig.REGION_ID, ClientConfig.ENV);
         }

         return true;
      }
   }

   protected String call0(String reqId, ClientToServerRpcMetadata rpcMetadata, String jsonParam) {
      if (rpcMetadata == null) {
         LoggerInit.LOGGER.warn(String.format("RPC_RPC_METADATA_NULL_EXCEPTION, reqId:%d, outerReqId:%s", 12315L, reqId));
         throw new AgwRpcException(RpcResultCodeEnum.RPC_RPC_METADATA_NULL_EXCEPTION);
      } else {
         rpcMetadata.check();
         if (AgwStringUtil.isBlank(jsonParam)) {
            LoggerInit.LOGGER.warn(String.format("RPC_REQUEST_BODY_NULL_EXCEPTION, reqId:%d, outerReqId:%s", 12315L, reqId));
            throw new AgwRpcException(RpcResultCodeEnum.RPC_REQUEST_BODY_NULL_EXCEPTION);
         } else {
            ClientConnection connection = ClientConnectionPool.getInstance().getConnection(ClientConfig.TLS);
            if (connection == null) {
               if (ClientConfig.TLS) {
                  OssCert.createOrUpdateClientCert(ClientConfig.REGION_ID, ClientConfig.ENV);
                  connection = ClientConnectionPool.getInstance().getConnection(ClientConfig.TLS);
                  if (connection == null) {
                     LoggerInit.LOGGER.warn(String.format("RPC_USER_CONNECTION_LOST, reqId:%d, outerReqId:%s", 12315L, reqId));
                     throw new AgwRpcException(RpcResultCodeEnum.RPC_USER_CONNECTION_LOST);
                  }
               }

               LoggerInit.LOGGER.warn(String.format("RPC_USER_CONNECTION_LOST, reqId:%d, outerReqId:%s", 12315L, reqId));
               throw new AgwRpcException(RpcResultCodeEnum.RPC_USER_CONNECTION_LOST);
            } else {
               int timeoutMs = rpcMetadata.getOptionalTimeoutMs() <= 4000 ? 4000 : rpcMetadata.getOptionalTimeoutMs();
               int connectionId = connection.getConnectionId();
               AgwMessage request = AgwMessageFactory.createAgwMessage(ReqBizMessageBuilder.getInstance(), this.clientVpcId, this.clientIpLong, this.clientProcessFlag, rpcMetadata.getServerHandlerName(), connectionId, rpcMetadata.getServerName(), timeoutMs, rpcMetadata.isRequestCompress(), rpcMetadata.isResponseCompress(), jsonParam);
               if (request == null) {
                  LoggerInit.LOGGER.warn(String.format("RPC_BUILD_MESSAGE_WRONG, reqId:%d, outerReqId:%s", 12315L, reqId));
                  throw new AgwRpcException(RpcResultCodeEnum.RPC_BUILD_MESSAGE_WRONG);
               } else {
                  request.getHeader().setOuterReqId(reqId);
                  request.mark("client_call_gateway");
                  request.mark("before_call");
                  AgwMessage response = connection.writeAndFlushSync(request);
                  request.mark("after_call");
                  if (response == null) {
                     LoggerInit.LOGGER.warn(String.format("RPC_USER_TO_GW_RESULT_EMPTY_EXCEPTION, reqId:%d, outerReqId:%s", request.getHeader().getReqId(), reqId));
                     request.mark("call_gateway_exception");
                     LoggerInit.LOGGER_PERF.info(request.getPerf());
                     throw new AgwRpcException(RpcResultCodeEnum.RPC_USER_TO_GW_RESULT_EMPTY_EXCEPTION);
                  } else {
                     AgwMessageHeader responseHeader = response.getHeader();
                     if (responseHeader.getInnerCode() != RpcResultCodeEnum.RPC_OK.getCode()) {
                        LoggerInit.LOGGER.warn(String.format("code:%d, msg:%s, reqId:%d, outerReqId:%s", responseHeader.getInnerCode(), responseHeader.getInnerMsg(), request.getHeader().getReqId(), reqId));
                        request.mark("call_gateway_exception");
                        LoggerInit.LOGGER_PERF.info(request.getPerf());
                        throw new AgwRpcException(responseHeader.getInnerCode(), responseHeader.getInnerMsg());
                     } else {
                        request.mark("after_call");
                        LoggerInit.LOGGER_PERF.info(request.getPerf());
                        return response.getBody();
                     }
                  }
               }
            }
         }
      }
   }

   protected String callWithCallBack0(String reqId, ClientToServerRpcMetadata rpcMetadata, String jsonParam, ResponseCallback callback) {
      throw new AgwRpcException(RpcResultCodeEnum.RPC_UNSUPPORT_OPERATION);
   }

   protected void destroy0() {
   }

   public static AgwClient getClient() {
      return client;
   }

   public static void setClient(AgwClient client) {
      client = client;
   }

   public String getClientVpcId() {
      return this.clientVpcId;
   }

   public void setClientVpcId(String clientVpcId) {
      this.clientVpcId = clientVpcId;
   }

   public String getClientIp() {
      return this.clientIp;
   }

   public void setClientIp(String clientIp) {
      this.clientIp = clientIp;
   }

   public String getClientProcessFlag() {
      return this.clientProcessFlag;
   }

   public void setClientProcessFlag(String clientProcessFlag) {
      this.clientProcessFlag = clientProcessFlag;
   }

   public long getClientIpLong() {
      return this.clientIpLong;
   }

   public void setClientIpLong(long clientIpLong) {
      this.clientIpLong = clientIpLong;
   }

   public static void main(String[] args) {
   }
}
