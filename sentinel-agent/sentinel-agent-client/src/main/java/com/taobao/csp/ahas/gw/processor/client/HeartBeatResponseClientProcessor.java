package com.taobao.csp.ahas.gw.processor.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.processor.IProcessor;

public class HeartBeatResponseClientProcessor implements IProcessor {
   private static HeartBeatResponseClientProcessor instance = new HeartBeatResponseClientProcessor();

   private HeartBeatResponseClientProcessor() {
   }

   public static IProcessor getInstance() {
      return instance;
   }

   public void process(Connection connection, AgwMessage request) {
      connection.notifySyncWrite(request);
   }
}
