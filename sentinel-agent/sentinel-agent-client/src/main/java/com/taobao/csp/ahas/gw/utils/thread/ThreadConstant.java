package com.taobao.csp.ahas.gw.utils.thread;

public class ThreadConstant {
   public static final int SERVER_DATAPROCESS_CORE_POOL_SIZE = 100;
   public static final int SERVER_DATAPROCESS_MAXIMUM_POOL_SIZE = 200;
   public static final long SERVER_DATAPROCESS_KEEP_ALIVE_TIME = 20L;
   public static final int SERVER_DATAPROCESS_QUEUE_SIZE = 800;
   public static final String SERVER_DATAPROCESS_THREAD_POOL_NAME = "AgwBizDataProcessProcessor - DEFAULT";
   public static final int SERVER_TOPOLOGY_CORE_POOL_SIZE = 75;
   public static final int SERVER_TOPOLOGY_MAXIMUM_POOL_SIZE = 150;
   public static final long SERVER_TOPOLOGY_KEEP_ALIVE_TIME = 20L;
   public static final int SERVER_TOPOLOGY_QUEUE_SIZE = 600;
   public static final String SERVER_TOPOLOGY_THREAD_POOL_NAME = "AgwBizTopologyProcessor - DEFAULT";
   public static final int SERVER_TOPOLOGYHEARTBEAT_CORE_POOL_SIZE = 75;
   public static final int SERVER_TOPOLOGYHEARTBEAT_MAXIMUM_POOL_SIZE = 150;
   public static final long SERVER_TOPOLOGYHEARTBEAT_KEEP_ALIVE_TIME = 20L;
   public static final int SERVER_TOPOLOGYHEARTBEAT_QUEUE_SIZE = 600;
   public static final String SERVER_TOPOLOGYHEARTBEAT_THREAD_POOL_NAME = "AgwBizTopologyHeartBeatProcessor - DEFAULT";
   public static final int SERVER_GATEWAYCERTIFICATE_CORE_POOL_SIZE = 5;
   public static final int SERVER_GATEWAYCERTIFICATE_MAXIMUM_POOL_SIZE = 10;
   public static final long SERVER_GATEWAYCERTIFICATE_KEEP_ALIVE_TIME = 20L;
   public static final int SERVER_GATEWAYCERTIFICATE_QUEUE_SIZE = 40;
   public static final String SERVER_GATEWAYCERTIFICATE_THREAD_POOL_NAME = "AgwBizServerGatewayCertificateProcessor - DEFAULT";
   public static final int CLIENT_CORE_POOL_SIZE = 2;
   public static final int CLIENT_MAXIMUM_POOL_SIZE = 10;
   public static final long CLIENT_KEEP_ALIVE_TIME = 20L;
   public static final int CLIENT_QUEUE_SIZE = 16;
   public static final String CLIENT_THREAD_POOL_NAME = "AgwBizProcessor - DEFAULT";
   public static final int EXPECTED_MAX_THREAD_POOL_INIT_TIME_MS = 500;
}
