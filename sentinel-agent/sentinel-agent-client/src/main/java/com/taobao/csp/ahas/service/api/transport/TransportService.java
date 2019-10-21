package com.taobao.csp.ahas.service.api.transport;

import com.taobao.csp.ahas.service.api.client.ClientInfoService;
import com.taobao.csp.ahas.service.exception.AhasClientException;
import com.taobao.csp.ahas.transport.api.AgwRequestHandler;
import com.taobao.csp.ahas.transport.api.AgwRequestUri;
import com.taobao.csp.ahas.transport.api.Request;
import com.taobao.csp.ahas.transport.api.Response;

public interface TransportService {
   String NAME = "transport";

   void init(ClientInfoService var1) throws AhasClientException;

   void registerHandler(String var1, AgwRequestHandler var2);

   <R> Response<R> invoke(AgwRequestUri var1, Request var2, Class<?> var3);

   void destroy();
}
