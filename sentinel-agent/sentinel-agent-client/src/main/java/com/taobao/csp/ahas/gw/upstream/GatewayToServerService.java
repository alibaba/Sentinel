package com.taobao.csp.ahas.gw.upstream;

import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;

public interface GatewayToServerService {
   AgwMessage call(AgwMessage var1);
}
