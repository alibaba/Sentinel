package com.taobao.csp.ahas.service.heartbeat;

import com.taobao.csp.ahas.service.api.client.ClientInfoService;
import com.taobao.csp.ahas.service.api.transport.TransportService;
import com.taobao.csp.ahas.service.time.AbstractTimerService;
import com.taobao.csp.ahas.transport.api.AgwRequestUri;
import com.taobao.csp.ahas.transport.api.Request;
import com.taobao.csp.ahas.transport.api.Response;
import com.taobao.csp.ahas.transport.api.ServiceConstants;

public class DefaultHeartbeatService extends AbstractTimerService implements HeartbeatService {
   private TransportService transportService;
   private ClientInfoService clientInfoService;
   private HeartbeatService heartbeatService;

   public DefaultHeartbeatService() {
      super("heartbeat");
   }

   public void init(ClientInfoService clientInfoService, TransportService transportService) {
      this.init(clientInfoService, transportService, this);
   }

   public void init(ClientInfoService clientInfoService, TransportService transportService, HeartbeatService heartbeatService) {
      this.clientInfoService = clientInfoService;
      this.transportService = transportService;
      this.heartbeatService = heartbeatService;
      super.init();
   }

   public Response<String> sendHeartbeat() {
      String version = this.clientInfoService.getVersion();

      try {
         Request request = new Request(true);
         request.addHeader("uid", this.clientInfoService.getUserId());
         request.addHeader("v", version);
         Response<String> response = this.transportService.invoke(new AgwRequestUri(ServiceConstants.Topology.HEARTBEAT), request, String.class);
         return response;
      } catch (Exception var4) {
         return Response.ofFailure(Response.Code.SERVER_ERROR, var4.getMessage());
      }
   }

   public void onTime() {
      this.heartbeatService.sendHeartbeat();
   }
}
