package com.taobao.csp.ahas.gw.processor;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;

public interface IProcessor {
   void process(Connection var1, AgwMessage var2);
}
