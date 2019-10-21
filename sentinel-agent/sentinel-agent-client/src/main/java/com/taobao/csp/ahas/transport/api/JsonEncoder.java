package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.third.com.alibaba.fastjson.JSON;

public class JsonEncoder implements Encoder<Object, String> {
   public String encode(Object object) throws EncoderException {
      try {
         return JSON.toJSONString(object);
      } catch (Exception var3) {
         throw new EncoderException(var3);
      }
   }
}
