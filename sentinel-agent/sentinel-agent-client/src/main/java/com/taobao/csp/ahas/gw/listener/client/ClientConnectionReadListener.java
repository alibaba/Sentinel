package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.processor.IProcessor;
import com.taobao.csp.ahas.gw.processor.client.ClientProcessorFactory;

public class ClientConnectionReadListener extends ClientConnectionListenerEmptyImpl {
   public void read(Connection connection, AgwMessage message) {
      AgwMessageHeader header = message.getHeader();
      IProcessor processor = ClientProcessorFactory.getInstance().generateProcessor(header.getMessageType(), header.getMessageDirection());
      if (processor == null) {
         LoggerInit.LOGGER.warn(String.format("can not get processor, conection:%s, reqId:%d, outerReqId:%s", connection.info(), header.getReqId(), header.getOuterReqId()));
      }

      try {
         processor.process(connection, message);
      } catch (Throwable var6) {
         LoggerInit.LOGGER.warn(String.format("process wrong, conection:%s, reqId:%d, outerReqId:%s", connection.info(), header.getReqId(), header.getOuterReqId()), var6);
      }

   }
}
