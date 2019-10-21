package com.taobao.csp.ahas.gw.utils.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ThreadPoolManager {
   private static ThreadPoolManager instance = new ThreadPoolManager();
   private ThreadPoolExecutor serverDataProcessPool;
   private ThreadPoolExecutor serverTopologyPool;
   private ThreadPoolExecutor serverTopologyHeartbeatPool;
   private ThreadPoolExecutor clientPool;
   private AtomicBoolean serverDataProcessPoolInited = new AtomicBoolean();
   private AtomicBoolean serverTopologyPoolInited = new AtomicBoolean();
   private AtomicBoolean serverTopologyHeartbeatPoolInited = new AtomicBoolean();
   private AtomicBoolean clientPoolInited = new AtomicBoolean();

   private ThreadPoolManager() {
   }

   public static ThreadPoolManager getInstance() {
      return instance;
   }

   private void initServerDataProcessThreadPool() {
      int coreSize = 100;
      int maxSize = 200;
      long keepAliveTime = 20L;
      ThreadFactory threadFactory = new NamedThreadFactory("AgwBizDataProcessProcessor - DEFAULT");
      this.serverDataProcessPool = new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue(800), threadFactory);
   }

   private void initServerTopologyThreadPool() {
      int coreSize = 75;
      int maxSize = 150;
      long keepAliveTime = 20L;
      ThreadFactory threadFactory = new NamedThreadFactory("AgwBizTopologyProcessor - DEFAULT");
      this.serverTopologyPool = new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue(600), threadFactory);
   }

   private void initServerTopologyHeartbeatThreadPool() {
      int coreSize = 75;
      int maxSize = 150;
      long keepAliveTime = 20L;
      ThreadFactory threadFactory = new NamedThreadFactory("AgwBizTopologyHeartBeatProcessor - DEFAULT");
      this.serverTopologyHeartbeatPool = new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue(600), threadFactory);
   }

   public ThreadPoolExecutor getServerDataProcessThreadPool() {
      if (this.serverDataProcessPoolInited.get()) {
         return this.serverDataProcessPool;
      } else if (this.serverDataProcessPoolInited.compareAndSet(false, true)) {
         this.initServerDataProcessThreadPool();
         return this.serverDataProcessPool;
      } else {
         try {
            TimeUnit.MILLISECONDS.sleep(500L);
         } catch (InterruptedException var2) {
         }

         return this.serverDataProcessPool;
      }
   }

   public ThreadPoolExecutor getServerTopologyThreadPool() {
      if (this.serverTopologyPoolInited.get()) {
         return this.serverTopologyPool;
      } else if (this.serverTopologyPoolInited.compareAndSet(false, true)) {
         this.initServerTopologyThreadPool();
         return this.serverTopologyPool;
      } else {
         try {
            TimeUnit.MILLISECONDS.sleep(500L);
         } catch (InterruptedException var2) {
         }

         return this.serverTopologyPool;
      }
   }

   public ThreadPoolExecutor getServerTopologyHeartbeatThreadPool() {
      if (this.serverTopologyHeartbeatPoolInited.get()) {
         return this.serverTopologyHeartbeatPool;
      } else if (this.serverTopologyHeartbeatPoolInited.compareAndSet(false, true)) {
         this.initServerTopologyHeartbeatThreadPool();
         return this.serverTopologyHeartbeatPool;
      } else {
         try {
            TimeUnit.MILLISECONDS.sleep(500L);
         } catch (InterruptedException var2) {
         }

         return this.serverTopologyHeartbeatPool;
      }
   }

   private void initClientThreadPool() {
      int coreSize = 2;
      int maxSize = 10;
      long keepAliveTime = 20L;
      ThreadFactory threadFactory = new NamedThreadFactory("AgwBizProcessor - DEFAULT");
      this.clientPool = new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue(16), threadFactory);
   }

   public ThreadPoolExecutor getClientThreadPool() {
      if (this.clientPoolInited.get()) {
         return this.clientPool;
      } else if (this.clientPoolInited.compareAndSet(false, true)) {
         this.initClientThreadPool();
         return this.clientPool;
      } else {
         try {
            TimeUnit.MILLISECONDS.sleep(500L);
         } catch (InterruptedException var2) {
         }

         return this.clientPool;
      }
   }
}
