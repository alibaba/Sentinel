package com.taobao.csp.ahas.transport.api;

import java.util.UUID;

public class AgwRequestUri implements RequestUri {
   private static final String DELIMITER = "_";
   private String serverName;
   private String handlerName;
   private String vpcId;
   private String ip;
   private String uri;
   private String tag;
   private String requestId;

   public AgwRequestUri(ServiceConstants service) {
      this(service, (String)null, (String)null, (String)null);
   }

   public AgwRequestUri(ServiceConstants service, String vpcId, String ip, String tag) {
      if (service != null) {
         this.serverName = service.getServerName();
         this.handlerName = service.getHandlerName();
      }

      this.vpcId = vpcId;
      this.ip = ip;
      this.uri = this.createUri(this.serverName, this.handlerName, vpcId, ip, tag);
      this.tag = tag;
      this.requestId = UUID.randomUUID().toString();
   }

   public String getServerName() {
      return this.serverName;
   }

   public String getHandlerName() {
      return this.handlerName;
   }

   public String getVpcId() {
      return this.vpcId;
   }

   public void setVpcId(String vpcId) {
      this.vpcId = vpcId;
   }

   public String getIp() {
      return this.ip;
   }

   public void setIp(String ip) {
      this.ip = ip;
   }

   public String getUri() {
      return this.uri;
   }

   public String getTag() {
      return this.tag;
   }

   private String createUri(String... parameters) {
      return this.join(parameters);
   }

   public String getRequestId() {
      return this.requestId;
   }

   private String createTag(String... parameters) {
      return this.join(parameters);
   }

   private String join(String... parameters) {
      StringBuilder sb = new StringBuilder();
      String[] arr$ = parameters;
      int len$ = parameters.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String parameter = arr$[i$];
         if (parameter != null) {
            sb.append(parameter).append("_");
         }
      }

      if (sb.length() > 0) {
         return sb.substring(0, sb.lastIndexOf("_"));
      } else {
         return sb.toString();
      }
   }
}
