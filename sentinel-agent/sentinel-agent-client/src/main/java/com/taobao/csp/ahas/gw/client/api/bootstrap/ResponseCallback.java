package com.taobao.csp.ahas.gw.client.api.bootstrap;

import com.taobao.csp.ahas.gw.client.api.exception.AgwException;

public interface ResponseCallback {
   void onAppException(Throwable var1);

   void onAppResponse(Object var1);

   void onAGWException(AgwException var1);
}
