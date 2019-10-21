package com.taobao.csp.ahas.service.api.thread;

import java.util.concurrent.*;

public class ThreadPoolService {
   private static final ExecutorService serviceListenerThreadPool = createServiceListenerThreadPool();

   public static ScheduledExecutorService createSingleThreadScheduledExecutor(String threadName) {
      return new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(threadName), getLoggerRejectedExecutionHandler());
   }

   public static Thread createClientMainThread(Runnable runnable) {
      return (new DefaultThreadFactory("main")).newThread(runnable);
   }

   private static ExecutorService createServiceListenerThreadPool() {
      return new ThreadPoolExecutor(1, 3, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue(1000), new DefaultThreadFactory("service-listener"), getLoggerRejectedExecutionHandler());
   }

   private static RejectedExecutionHandler getLoggerRejectedExecutionHandler() {
      return new RejectedExecutionHandler() {
         public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
         }
      };
   }

   public static ExecutorService getServiceListenerThreadPool() {
      return serviceListenerThreadPool;
   }

   public static ExecutorService createLogstashThreadPool() {
      return new ThreadPoolExecutor(1, 4, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue(1000), new DefaultThreadFactory("logstash-report"), getLoggerRejectedExecutionHandler());
   }
}
