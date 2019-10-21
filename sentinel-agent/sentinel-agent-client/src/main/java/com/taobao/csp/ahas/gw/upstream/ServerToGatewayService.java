package com.taobao.csp.ahas.gw.upstream;

import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;

public interface ServerToGatewayService {
   AgwMessage call(AgwMessage var1);

   AgwMessage callWithCallBack(AgwMessage var1);
}
