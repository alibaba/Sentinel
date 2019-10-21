package com.taobao.csp.ahas.transport.api;

public interface ServiceConstants {
   String getServerName();

   String getHandlerName();

   public static enum MonkeyKing implements ServiceConstants {
      CHAOS_BLADE("chaosblade"),
      NAME("noop");

      private String handlerName;

      private MonkeyKing(String handlerName) {
         this.handlerName = handlerName;
      }

      public String getServerName() {
         return "MonkeyKing";
      }

      public String getHandlerName() {
         return this.handlerName;
      }
   }

   public static enum Sentinel implements ServiceConstants {
      GET_RULES("getRules"),
      JSON_TREE("jsonTree"),
      CLUSTER_NODE("clusterNode"),
      GET_SWITCH("getSwitch"),
      SET_SWITCH("setSwitch"),
      METRIC("metric"),
      VERSION("version"),
      GET_PARAM_RULES("getParamFlowRules"),
      GET_CLUSTER_MODE("getClusterMode"),
      SET_CLUSTER_MODE("setClusterMode"),
      MODIFY_CLUSTER_CLIENT_CONFIG("cluster/client/modifyConfig"),
      GET_CLUSTER_CLIENT_INFO("cluster/client/fetchConfig"),
      GET_CLUSTER_SERVER_INFO("cluster/server/info"),
      MODIFY_CLUSTER_SERVER_FLOW_CONFIG("cluster/server/modifyFlowConfig"),
      MODIFY_CLUSTER_SERVER_NAMESPACE_SET("cluster/server/modifyNamespaceSet"),
      NAME("noop");

      private String handlerName;

      private Sentinel(String handlerName) {
         this.handlerName = handlerName;
      }

      public String getServerName() {
         return "Sentinel";
      }

      public String getHandlerName() {
         return this.handlerName;
      }
   }

   public static enum DataProcess implements ServiceConstants {
      SAR_LOG("sarLog"),
      PROCESS_LOG("processLog"),
      NETWORK_LOG("networkLog"),
      AHAS_LOG("ahasLog"),
      JAVA_AGENT_LOG("javaAgentLog"),
      JAVA_SDK_LOG("javaSDKLog"),
      NAME("noop"),
      CONTAINER_EVENT("containerEvent");

      private String handlerName;

      private DataProcess(String handlerName) {
         this.handlerName = handlerName;
      }

      public String getServerName() {
         return "DataProcess";
      }

      public String getHandlerName() {
         return this.handlerName;
      }
   }

   public static enum Topology implements ServiceConstants {
      MK_TOPIC("topic"),
      FAULT_INJECT("faultInject"),
      SERVICE_SWITCH("switch"),
      UPGRADE("upgrade"),
      CONNECT("connect"),
      HEARTBEAT("heartbeat"),
      CONSTANTS("containers"),
      CLOSE("close"),
      AGENT_EVENT("agentEvent"),
      UPGRADE_CALLBACK("upgradeCallback"),
      KUBERNETES("kubernetes"),
      NAME("noop");

      private String handlerName;

      private Topology(String handlerName) {
         this.handlerName = handlerName;
      }

      public String getServerName() {
         return "Topology";
      }

      public String getHandlerName() {
         return this.handlerName;
      }
   }
}
