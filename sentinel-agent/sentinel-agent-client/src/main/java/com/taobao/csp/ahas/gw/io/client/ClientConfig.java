package com.taobao.csp.ahas.gw.io.client;

import com.taobao.csp.ahas.gw.logger.LoggerInit;

public class ClientConfig {
   public static final int CLIENT_CONNECTION_POOL_MAX_SIZE = 2;
   public static int CLIENT_CONNECT_PORT = 9527;
   public static String CLIENT_CONNECT_IP = "47.98.243.171";
   public static boolean TLS = false;
   public static String REGION_ID;
   public static String ENV;
   public static final int SEND_HB_MAX_IDLE_TIME_SEC = 10;

   public static void init(String ip, int port, boolean tls, String regionId, String env) {
      LoggerInit.LOGGER.info(String.format("init ahas gateway ip : %s, port : %d", ip, port));
      CLIENT_CONNECT_IP = ip;
      CLIENT_CONNECT_PORT = port;
      TLS = tls;
      REGION_ID = regionId;
      ENV = env;
   }
}
