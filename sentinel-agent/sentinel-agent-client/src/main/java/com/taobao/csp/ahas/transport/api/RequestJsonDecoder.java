package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.third.com.alibaba.fastjson.JSON;

public class RequestJsonDecoder implements Decoder<String, Request> {
   public Request decode(String json) throws DecoderException {
      try {
         return (Request)JSON.parseObject(json, Request.class);
      } catch (Exception var3) {
         throw new DecoderException(var3);
      }
   }
}
