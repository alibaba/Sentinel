package com.taobao.csp.ahas.gw.processor.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.processor.IProcessor;

public class BizResponseClientProcessor implements IProcessor {
   private static BizResponseClientProcessor instance = new BizResponseClientProcessor();

   private BizResponseClientProcessor() {
   }

   public static BizResponseClientProcessor getInstance() {
      return instance;
   }

   public void process(Connection connection, AgwMessage request) {
      connection.notifySyncWrite(request);
   }
}
