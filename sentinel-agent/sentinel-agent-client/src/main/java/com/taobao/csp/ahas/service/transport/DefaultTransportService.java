package com.taobao.csp.ahas.service.transport;

import com.taobao.csp.ahas.auth.api.AuthException;
import com.taobao.csp.ahas.auth.api.AuthUtil;
import com.taobao.csp.ahas.gw.client.bootstrap.outer.AgwClient;
import com.taobao.csp.ahas.gw.client.bootstrap.outer.ClientToServerAgwConfig;
import com.taobao.csp.ahas.service.api.client.ClientInfoService;
import com.taobao.csp.ahas.service.api.transport.TransportService;
import com.taobao.csp.ahas.service.exception.AhasClientException;
import com.taobao.csp.ahas.transport.api.*;

import java.util.Map;

public class DefaultTransportService implements TransportService {
   private static final int RETRY_COUNT = 3;
   private RequestInterceptor requestInterceptor;
   private ClientInfoService clientInfoService;
   private AgwClient agwClient = AgwClient.getInstance();
   private AgwRequestInvoker agwRequestInvoker;

   public void init(ClientInfoService clientInfoService) throws AhasClientException {
      this.clientInfoService = clientInfoService;
      this.initAgwClient();
      this.initRequestInvoker();
      this.connect();
   }

   private void initRequestInvoker() {
      this.requestInterceptor = new TimestampInterceptor();
      ((TimestampInterceptor)this.requestInterceptor).setInterceptor(new ClientAuthInterceptor());
      this.agwRequestInvoker = new AgwRequestClientInvoker(this.agwClient);
      this.agwRequestInvoker.setRequestInterceptor(this.requestInterceptor);
   }

   private void initAgwClient() throws AhasClientException {
      if (this.clientInfoService.getPrivateIp() == null) {
         throw new AhasClientException("Cannot get local ip.");
      } else {
         String tag = this.clientInfoService.getType() + ":" + this.clientInfoService.getPrivateIp() + ":" + this.clientInfoService.getPid();
         ClientToServerAgwConfig agwConfig = new ClientToServerAgwConfig();
         agwConfig.setClientVpcId(this.clientInfoService.getVpcId()).setClientIp(this.clientInfoService.getHostIp()).setClientProcessFlag(tag).setAhasGatewayIp(this.clientInfoService.getGatewayHost()).setAhasGatewayPort(this.clientInfoService.getGatewayPort()).setOptionalTimeoutMs(3000);
         this.agwClient.init(agwConfig);
      }
   }

   private void connect() throws AhasClientException {
      Request request = new Request(true);
      String deviceId;
      if (this.clientInfoService.getDeviceType() == 0) {
         deviceId = this.clientInfoService.getInstanceId();
      } else {
         deviceId = this.clientInfoService.getHostname();
      }

      request.addParam("vpcId", this.clientInfoService.getVpcId()).addParam("ip", this.clientInfoService.getPrivateIp()).addParam("appName", this.clientInfoService.getAppName()).addParam("appType", this.clientInfoService.getAppType()).addParam("ahasAppName", this.clientInfoService.getAhasAppName()).addParam("pid", this.clientInfoService.getPid()).addParam("type", this.clientInfoService.getType()).addParam("namespace", this.clientInfoService.getNamespace()).addParam("deviceId", deviceId).addParam("deviceType", this.clientInfoService.getDeviceType() + "").addParam("hostIp", this.clientInfoService.getHostIp()).addParam("v", this.clientInfoService.getVersion());
      if (this.clientInfoService.isPrivate()) {
         request.addParam("uid", this.clientInfoService.getUserId());
      } else {
         request.addParam("ak", this.clientInfoService.getLicense());
      }

      AgwRequestUri requestUri = new AgwRequestUri(ServiceConstants.Topology.CONNECT);
      AgwRequestClientInvoker clientInvoker = new AgwRequestClientInvoker(this.agwClient);
      AhasClientException panicException = null;
      int count = 0;

      while(true) {
         ++count;

         try {
            Response<Map<String, String>> response = this.invoke0(requestUri, request, clientInvoker, Map.class);
            this.handleConnectResponse(response);
            panicException = null;
         } catch (AhasClientException var8) {
            panicException = var8;
         }

         if (panicException == null) {
            return;
         }

         if (count >= 3) {
            throw panicException;
         }

         try {
            Thread.sleep(500L);
         } catch (InterruptedException var9) {
         }
      }
   }

   private void handleConnectResponse(Response<Map<String, String>> response) throws AhasClientException {
      if (!response.isSuccess()) {
         throw new AhasClientException("Connect server failed, " + response.getError());
      } else {
         Map<String, String> result = (Map)response.getResult();
         this.clientInfoService.setAid((String)result.get("aid"));
         String accessKey = (String)result.get("ak");
         String secretKey = (String)result.get("sk");
         String tid = (String)result.get("tid");
         if (tid != null && tid.length() != 0) {
            if (!this.clientInfoService.isPrivate()) {
               String uid = (String)result.get("uid");
               if (uid == null || uid.length() == 0) {
                  throw new AhasClientException("uid is empty");
               }

               this.clientInfoService.setUserId(uid);
            }

            this.clientInfoService.setTid(tid);

            try {
               AuthUtil.recordKeyToFile(accessKey, secretKey);
            } catch (Exception var7) {
               throw new AhasClientException("Record ak and sk exception", var7);
            }
         } else {
            throw new AhasClientException("tid is empty");
         }
      }
   }

   public void destroy() {
      if (this.agwClient != null) {
         this.agwClient.destroy();
      }

   }

   public void registerHandler(String handlerName, AgwRequestHandler handler) {
      handler.setRequestInterceptor(this.requestInterceptor);
      this.agwClient.addHandler(handlerName, handler);
   }

   public <R> Response<R> invoke(AgwRequestUri uri, Request request, Class<?> clazz) {
      return this.invoke0(uri, request, this.agwRequestInvoker, clazz);
   }

   private <R> Response<R> invoke0(AgwRequestUri uri, Request request, AgwRequestInvoker invoker, Class<?> clazz) {
      if (invoker == null) {
         return Response.ofFailure(Response.Code.UNINITIALIZED, "request invoker has not been initialized yet");
      } else {
         try {
            String userId = this.clientInfoService.getUserId();
            if (userId != null && userId != "") {
               request.addHeader("uid", userId);
            }

            request.addHeader("aid", this.clientInfoService.getAid());
            request.addHeader("pid", this.clientInfoService.getPid());
            request.addHeader("type", this.clientInfoService.getType());
            return invoker.invoke(uri, request, clazz);
         } catch (Exception var7) {
            String message = "aid: " + this.clientInfoService.getAid() + ", msg: " + var7.getMessage();
            if (var7 instanceof TimeoutException) {
               return Response.ofFailure(Response.Code.TIMEOUT, message);
            } else if (var7 instanceof AuthException) {
               return Response.ofFailure(Response.Code.FORBIDDEN, message);
            } else if (var7 instanceof EncoderException) {
               return Response.ofFailure(Response.Code.ENCODE_ERROR, message);
            } else {
               return var7 instanceof DecoderException ? Response.ofFailure(Response.Code.DECODE_ERROR, message) : Response.ofFailure(Response.Code.SERVER_ERROR, message);
            }
         }
      }
   }
}
