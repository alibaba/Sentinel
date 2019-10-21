package com.taobao.csp.ahas.service.transport;

import com.taobao.csp.ahas.gw.client.api.bootstrap.ClientToServerRpcMetadata;
import com.taobao.csp.ahas.gw.client.api.bootstrap.ResponseCallback;
import com.taobao.csp.ahas.gw.client.api.exception.AgwRpcException;
import com.taobao.csp.ahas.gw.client.bootstrap.outer.AgwClient;
import com.taobao.csp.ahas.transport.api.AgwRequestInvoker;
import com.taobao.csp.ahas.transport.api.AgwRequestUri;
import com.taobao.csp.ahas.transport.api.RequestException;

public class AgwRequestClientInvoker extends AgwRequestInvoker {
   private AgwClient agwClient;

   public AgwRequestClientInvoker(AgwClient agwClient) {
      this.agwClient = agwClient;
   }

   public String doInvoke(AgwRequestUri uri, String jsonParam) throws AgwRpcException {
      ClientToServerRpcMetadata rpcMetadata = new ClientToServerRpcMetadata();
      rpcMetadata.setServerName(uri.getServerName());
      rpcMetadata.setServerHandlerName(uri.getHandlerName());
      String result = this.agwClient.call(uri.getRequestId(), rpcMetadata, jsonParam);
      return result;
   }

   public void doAsyncInvoke(AgwRequestUri uri, String jsonParam, ResponseCallback callback) throws RequestException, AgwRpcException {
      throw new RequestException("Unsupported method");
   }
}
