package com.alibaba.csp.sentinel.datasource.acm;

import java.util.HashMap;
import java.util.Map;

public class SentinelAcmConstants {
   public static final String SENTINEL_GROUP_ID = "ahas-sentinel";
   public static final String SENTINEL_FLOW_RULE_DATA_ID_PREFIX = "flow-rule-";
   public static final String SENTINEL_DEGRADE_RULE_DATA_ID_PREFIX = "degrade-rule-";
   public static final String SENTINEL_SYSTEM_RULE_DATA_ID_PREFIX = "system-rule-";
   public static final String SENTINEL_PARAM_FLOW_RULE_DATA_ID_PREFIX = "param-flow-rule-";
   public static final String SENTINEL_AUTHORITY_RULE_DATA_ID_PREFIX = "authority-rule-";
   public static final String SENTINEL_CLUSTER_ASSIGN_MAP_DATA_ID_PREFIX = "cluster-assign-map-";
   public static final String SENTINEL_CLUSTER_CLIENT_CONFIG_DATA_ID_PREFIX = "cluster-client-config-";
   public static final String SENTINEL_CLUSTER_SERVER_FLOW_CONFIG_DATA_ID_PREFIX = "cluster-server-flow-config-";
   public static final String SENTINEL_CLUSTER_NAMESPACE_SET_DATA_ID_PREFIX = "cluster-namespace-set-";
   public static final String SENTINEL_GATEWAY_FLOW_RULE_DATA_ID_PREFIX = "gateway-flow-rule-";
   public static final String SENTINEL_GATEWAY_API_DEFINITION_DATA_ID_PREFIX = "gateway-api-definition-";
   private static final Map<String, String> endpoints = new HashMap(16);

   public static String getAcmEndpoint(String regionId) {
      return (String)endpoints.get(regionId);
   }

   public static String formFlowRuleDataId(String userId, String namespace, String appName) {
      return "flow-rule-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formDegradeRuleDataId(String userId, String namespace, String appName) {
      return "degrade-rule-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formSystemRuleDataId(String userId, String namespace, String appName) {
      return "system-rule-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formParamFlowRuleDataId(String userId, String namespace, String appName) {
      return "param-flow-rule-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formAuthorityRuleDataId(String userId, String namespace, String appName) {
      return "authority-rule-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formClusterAssignMapDataId(String userId, String namespace, String appName) {
      return "cluster-assign-map-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formClusterClientConfigDataId(String userId, String namespace, String appName) {
      return "cluster-client-config-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formClusterServerFlowConfigDataId(String userId, String namespace, String appName) {
      return "cluster-server-flow-config-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formClusterServerNamespaceSetDataId(String userId, String namespace, String appName) {
      return "cluster-namespace-set-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formGatewayFlowRuleDataId(String userId, String namespace, String appName) {
      return "gateway-flow-rule-" + userId + "-" + namespace + "-" + appName;
   }

   public static String formGatewayApiDefinitionDataId(String userId, String namespace, String appName) {
      return "gateway-api-definition-" + userId + "-" + namespace + "-" + appName;
   }

   static {
      endpoints.put("cn-qingdao", "addr-qd-internal.edas.aliyun.com");
      endpoints.put("cn-beijing", "addr-bj-internal.edas.aliyun.com");
      endpoints.put("cn-hangzhou", "addr-hz-internal.edas.aliyun.com");
      endpoints.put("cn-shanghai", "addr-sh-internal.edas.aliyun.com");
      endpoints.put("cn-shenzhen", "addr-sz-internal.edas.aliyun.com");
      endpoints.put("cn-hongkong", "addr-hk-internal.edas.aliyuncs.com");
      endpoints.put("ap-southeast-1", "addr-singapore-internal.edas.aliyun.com");
      endpoints.put("us-west-1", "addr-us-west-1-internal.acm.aliyun.com");
      endpoints.put("us-east-1", "addr-us-east-1-internal.acm.aliyun.com");
   }
}
