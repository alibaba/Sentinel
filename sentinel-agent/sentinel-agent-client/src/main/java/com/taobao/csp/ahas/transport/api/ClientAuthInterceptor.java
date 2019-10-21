package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.ahas.auth.api.AuthUtil;

public class ClientAuthInterceptor extends AuthInterceptor {
   public Tuple<String, String> getAkAndSk(Request request) {
      return new Tuple(AuthUtil.getAccessKey(), AuthUtil.getSecretKey());
   }
}
