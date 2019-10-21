package com.taobao.csp.ahas.gw.processor.client;

import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.processor.IProcessor;
import com.taobao.csp.ahas.gw.processor.IProcessorFactory;

public class ClientProcessorFactory implements IProcessorFactory {
   private static ClientProcessorFactory instance = new ClientProcessorFactory();

   private ClientProcessorFactory() {
   }

   public static ClientProcessorFactory getInstance() {
      return instance;
   }

   public IProcessor generateProcessor(byte messageType, byte messageDirection) {
      if (2 == messageType && 1 == messageDirection) {
         return BizRequestClientProcessor.getInstance();
      } else if (2 == messageType && 2 == messageDirection) {
         return BizResponseClientProcessor.getInstance();
      } else if (1 == messageType && 2 == messageDirection) {
         return HeartBeatResponseClientProcessor.getInstance();
      } else if (1 == messageType && 1 == messageDirection) {
         return HeartBeatRequestClientProcessor.getInstance();
      } else {
         LoggerInit.LOGGER.warn("can not handle message: messageType:%d, messageDirection:%d", messageType, messageDirection);
         return null;
      }
   }
}
