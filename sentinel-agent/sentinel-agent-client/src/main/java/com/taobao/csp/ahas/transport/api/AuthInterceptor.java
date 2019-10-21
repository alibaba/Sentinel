package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.ahas.auth.api.AuthException;
import com.taobao.csp.ahas.auth.api.AuthUtil;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializerFeature;

public abstract class AuthInterceptor extends RequestInterceptorChain {
   public static final String SIGN_DATA = "sd";
   public static final String SIGN_KEY = "sn";

   public void doHandle(Request request) throws RequestException {
      String sign = request.getHeader("sn");
      if (sign != null && sign.trim().length() != 0) {
         Tuple<String, String> akAndSk = this.getAkAndSk(request);
         String secretKey = (String)akAndSk.getR();
         if (secretKey != null && secretKey.length() != 0) {
            String signData = request.getHeader("sd");
            if (signData == null) {
               signData = JSON.toJSONString(request.getParams(), SerializerFeature.MapSortField);
            }

            boolean authResult = AuthUtil.auth(sign, secretKey, signData);
            if (!authResult) {
               throw new AuthException("Illegal request.");
            }
         } else {
            throw new AuthException("Illegal request. Cannot find secret key.");
         }
      } else {
         throw new AuthException("Sign must not be empty.");
      }
   }

   public void doInvoke(Request request) throws RequestException {
      String sign;
      try {
         Tuple<String, String> akAndSk = this.getAkAndSk(request);
         String secretKey = (String)akAndSk.getR();
         String signData = request.getHeader("sd");
         if (signData == null) {
            signData = JSON.toJSONString(request.getParams(), SerializerFeature.MapSortField);
         }

         sign = AuthUtil.sign(secretKey, signData);
      } catch (Exception var6) {
         throw new AuthException(var6);
      }

      request.addHeader("sn", sign);
   }

   public abstract Tuple<String, String> getAkAndSk(Request var1);
}
