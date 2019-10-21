package com.taobao.csp.ahas.gw.listener;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;

public interface IConnectionListener {
   void close(Connection var1);

   void exceptionCaught(Connection var1, Throwable var2);

   void idle(Connection var1);

   void write(Connection var1, AgwMessage var2);

   void writeSuccess(Connection var1, AgwMessage var2);

   void writeFail(Connection var1, AgwMessage var2, Throwable var3);

   void read(Connection var1, AgwMessage var2);
}
