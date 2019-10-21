package com.taobao.csp.ahas.gw.connection;

import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;

public interface Connection {
   boolean isInited();

   boolean init(AgwMessage var1);

   void writeAndFlush(AgwMessage var1);

   AgwMessage writeAndFlushSync(AgwMessage var1);

   void notifySyncWrite(AgwMessage var1);

   void handleSyncWriteError(AgwMessage var1);

   void close();

   String info();

   String uuid();
}
