package com.taobao.csp.ahas.gw.client.bootstrap.outer;

import com.taobao.csp.ahas.gw.client.api.bootstrap.BaseAgwConfig;

public class ClientToServerAgwConfig extends BaseAgwConfig<ClientToServerAgwConfig> {
   private String clientVpcId;
   private String clientIp;
   private String clientProcessFlag;
   private String ahasGatewayIp;
   private int ahasGatewayPort;
   private String regionId;
   private String env;
   private boolean tls;

   public String getClientVpcId() {
      return this.clientVpcId;
   }

   public ClientToServerAgwConfig setClientVpcId(String clientVpcId) {
      this.clientVpcId = clientVpcId;
      return this;
   }

   public String getClientIp() {
      return this.clientIp;
   }

   public ClientToServerAgwConfig setClientIp(String clientIp) {
      this.clientIp = clientIp;
      return this;
   }

   public String getClientProcessFlag() {
      return this.clientProcessFlag;
   }

   public ClientToServerAgwConfig setClientProcessFlag(String clientProcessFlag) {
      this.clientProcessFlag = clientProcessFlag;
      return this;
   }

   public String getAhasGatewayIp() {
      return this.ahasGatewayIp;
   }

   public ClientToServerAgwConfig setAhasGatewayIp(String ahasGatewayIp) {
      this.ahasGatewayIp = ahasGatewayIp;
      return this;
   }

   public int getAhasGatewayPort() {
      return this.ahasGatewayPort;
   }

   public ClientToServerAgwConfig setAhasGatewayPort(int ahasGatewayPort) {
      this.ahasGatewayPort = ahasGatewayPort;
      return this;
   }

   public boolean isTls() {
      return this.tls;
   }

   public ClientToServerAgwConfig setTls(boolean tls) {
      this.tls = tls;
      return this;
   }

   public String getRegionId() {
      return this.regionId;
   }

   public ClientToServerAgwConfig setRegionId(String regionId) {
      this.regionId = regionId;
      return this;
   }

   public String getEnv() {
      return this.env;
   }

   public ClientToServerAgwConfig setEnv(String env) {
      this.env = env;
      return this;
   }
}
