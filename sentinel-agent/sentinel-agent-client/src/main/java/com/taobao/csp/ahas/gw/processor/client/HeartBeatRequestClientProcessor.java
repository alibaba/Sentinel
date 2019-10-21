package com.taobao.csp.ahas.gw.processor.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import com.taobao.csp.ahas.gw.processor.IProcessor;

public class HeartBeatRequestClientProcessor implements IProcessor {
   private static HeartBeatRequestClientProcessor instance = new HeartBeatRequestClientProcessor();

   private HeartBeatRequestClientProcessor() {
   }

   public static IProcessor getInstance() {
      return instance;
   }

   public void process(Connection connection, AgwMessage request) {
      AgwMessageHeader header = request.getHeader();
      if (!connection.isInited()) {
         connection.init(request);
      }

      header.setMessageDirection((byte)2);
      connection.writeAndFlush(request);
   }
}
