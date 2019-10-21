package com.taobao.csp.ahas.transport.api;

import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Type;

public class ResponseJsonDecoder<R> implements Decoder<String, Response<R>> {
   private Class<?> clazz;

   public ResponseJsonDecoder(Class<?> clazz) {
      this.clazz = clazz;
   }

   public Response<R> decode(String json) throws DecoderException {
      try {
         return (Response)JSON.parseObject(json, new TypeReference<Response<R>>(new Type[]{this.clazz}) {
         });
      } catch (Exception var3) {
         throw new DecoderException(var3);
      }
   }
}
