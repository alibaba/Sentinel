package com.taobao.csp.ahas.gw.redis;

import com.taobao.middleware.logger.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AgwRedisUtil {
   public static final int EXPIRE_SEC = 20;
   public static final int MILLISECOND_PER_RESET = 10000;
   private static RedisConfig config = RedisConfig.getInstance();
   private static JedisPoolConfig jedisPoolConfig;
   private static JedisPool jedisPool;
   private static final AtomicBoolean initFlag = new AtomicBoolean(false);
   private static LoggerWapper logger;
   public static final int REDIS_RESULT_CODE_FAIL = -9999;
   public static final int REDIS_RESULT_CODE_OK = 0;
   public static String addConnectionGroupToGatewayLuaScript = "local g_key = KEYS[1];local cg_key = ARGV[1];local g_ip = ARGV[2];redis.call('sadd', g_key, cg_key);redis.call('sadd', cg_key, g_ip);return 0;";
   public static String removeConnectionGroupFromGatewayLuaScript = "local g_key = KEYS[1];local cg_key = ARGV[1];local g_ip = ARGV[2];redis.call('srem', g_key, cg_key);redis.call('srem', cg_key, g_ip);return 0;";

   public static void init(String redisHost, int redisPort, String redisPassword, Object realLogger) {
      if (initFlag.compareAndSet(false, true)) {
         logger = new LoggerWapper(realLogger);
         jedisPoolConfig = new JedisPoolConfig();
         jedisPoolConfig.setMaxTotal(config.getMaxTotal());
         jedisPoolConfig.setMaxIdle(config.getMaxIdle());
         jedisPoolConfig.setMinIdle(config.getMinIdle());
         jedisPoolConfig.setMaxWaitMillis((long)config.getMaxWaitMills());
         jedisPoolConfig.setTestOnBorrow(config.isTestOnBorrow());
         jedisPoolConfig.setTestOnReturn(config.isTestOnReturn());
         jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, config.getMaxWaitMills(), redisPassword);
      }
   }

   public static long addConnectionGroupToGateway(String gatewayKey, String connectionGroup, String gatewayIp) {
      return exec(addConnectionGroupToGatewayLuaScript, 1, gatewayKey, connectionGroup, gatewayIp);
   }

   public static long removeConnectionGroupFromGateway(String gatewayKey, String connectionGroup, String gatewayIp) {
      return exec(removeConnectionGroupFromGatewayLuaScript, 1, gatewayKey, connectionGroup, gatewayIp);
   }

   public static Set<String> getAllConnectionGroups(String gatewayKey) {
      if (!initFlag.get()) {
         throw new IllegalStateException("please init redis first");
      } else {
         Jedis jedis = null;

         HashSet var3;
         try {
            jedis = jedisPool.getResource();
            Set var2 = jedis.smembers(gatewayKey);
            return var2;
         } catch (Exception var7) {
            logger.warn(String.format("smembers wrong, key:%s", gatewayKey), var7);
            var3 = new HashSet();
         } finally {
            if (jedis != null) {
               jedis.close();
            }

         }

         return var3;
      }
   }

   public static Set<String> getGatewaysByConnectionGroupKey(String clientProcessConnectionKey) {
      if (!initFlag.get()) {
         throw new IllegalStateException("please init redis first");
      } else {
         Jedis jedis = null;

         Object var3;
         try {
            jedis = jedisPool.getResource();
            Set var2 = jedis.smembers(clientProcessConnectionKey);
            return var2;
         } catch (Exception var7) {
            logger.warn(String.format("getGatewaysByConnectionGroupKey wrong, key:%s", clientProcessConnectionKey), var7);
            var3 = null;
         } finally {
            if (jedis != null) {
               jedis.close();
            }

         }

         return (Set)var3;
      }
   }

   public static void set(String key, String value) {
      if (!initFlag.get()) {
         throw new IllegalStateException("please init redis first");
      } else {
         Jedis jedis = null;

         try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
         } catch (Exception var7) {
            logger.warn(String.format("set wrong, key:%s, value:%s", key, value), var7);
         } finally {
            if (jedis != null) {
               jedis.close();
            }

         }

      }
   }

   public static String get(String key) {
      if (!initFlag.get()) {
         throw new IllegalStateException("please init redis first");
      } else {
         Jedis jedis = null;

         Object var3;
         try {
            jedis = jedisPool.getResource();
            String var2 = jedis.get(key);
            return var2;
         } catch (Exception var7) {
            logger.warn(String.format("get wrong, key:%s", key), var7);
            var3 = null;
         } finally {
            if (jedis != null) {
               jedis.close();
            }

         }

         return (String)var3;
      }
   }

   private static long exec(String lua, int count, String... param) {
      if (!initFlag.get()) {
         logger.warn(String.format("redis uninited, key:%s", parseStringArray(param)));
         return -9999L;
      } else {
         Jedis jedis = null;

         long var7;
         try {
            jedis = jedisPool.getResource();
            long var4 = (Long)jedis.eval(lua, count, param);
            return var4;
         } catch (Exception var12) {
            logger.warn(String.format("eval lua wrong, param:%s", parseStringArray(param)), var12);
            var7 = -9999L;
         } finally {
            if (jedis != null) {
               jedis.close();
            }

         }

         return var7;
      }
   }

   private static String parseStringArray(String[] param) {
      StringBuffer sb = new StringBuffer(128);
      String[] var2 = param;
      int var3 = param.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String s = var2[var4];
         sb.append(s).append("@$");
      }

      return sb.toString();
   }

   static class LoggerWapper {
      private Logger mwLogger = null;
      private org.slf4j.Logger slfLogger = null;

      public LoggerWapper(Object logger) {
         if (logger instanceof Logger) {
            this.mwLogger = (Logger)logger;
         } else if (logger instanceof org.slf4j.Logger) {
            this.slfLogger = (org.slf4j.Logger)logger;
         } else {
            System.out.println(String.format("unknown logger:%s", logger.getClass().getCanonicalName()));
         }

      }

      public void warn(String message, Throwable t) {
         if (this.mwLogger != null) {
            this.mwLogger.warn(message, t);
         } else if (this.slfLogger != null) {
            this.slfLogger.warn(message, t);
         } else {
            System.out.println(message);
            t.printStackTrace();
         }

      }

      public void warn(String message) {
         if (this.mwLogger != null) {
            this.mwLogger.warn(message);
         } else if (this.slfLogger != null) {
            this.slfLogger.warn(message);
         } else {
            System.out.println(message);
         }

      }
   }
}
