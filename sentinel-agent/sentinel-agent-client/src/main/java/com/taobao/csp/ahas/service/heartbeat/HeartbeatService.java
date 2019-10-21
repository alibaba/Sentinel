package com.taobao.csp.ahas.service.heartbeat;

import com.taobao.csp.ahas.service.api.client.ClientInfoService;
import com.taobao.csp.ahas.service.api.transport.TransportService;
import com.taobao.csp.ahas.service.exception.AhasClientException;
import com.taobao.csp.ahas.transport.api.Response;

public interface HeartbeatService {
   String NAME = "heartbeat";

   void init(ClientInfoService var1, TransportService var2) throws AhasClientException;

   void init(ClientInfoService var1, TransportService var2, HeartbeatService var3) throws AhasClientException;

   Response<String> sendHeartbeat();
}
