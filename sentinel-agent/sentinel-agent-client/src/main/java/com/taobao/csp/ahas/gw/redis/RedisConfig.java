package com.taobao.csp.ahas.gw.redis;

public class RedisConfig {
   private static RedisConfig instance = new RedisConfig();
   private boolean testOnReturn = true;
   private int maxIdle = 48;
   private int minIdle = 12;
   private int maxWaitMills = 2000;
   private boolean testOnBorrow = true;
   private int maxTotal = 64;
   private long expiration = 1800L;

   public static RedisConfig getInstance() {
      return instance;
   }

   private RedisConfig() {
   }

   public boolean isTestOnReturn() {
      return this.testOnReturn;
   }

   public int getMaxIdle() {
      return this.maxIdle;
   }

   public int getMinIdle() {
      return this.minIdle;
   }

   public int getMaxWaitMills() {
      return this.maxWaitMills;
   }

   public boolean isTestOnBorrow() {
      return this.testOnBorrow;
   }

   public int getMaxTotal() {
      return this.maxTotal;
   }

   public long getExpiration() {
      return this.expiration;
   }
}
