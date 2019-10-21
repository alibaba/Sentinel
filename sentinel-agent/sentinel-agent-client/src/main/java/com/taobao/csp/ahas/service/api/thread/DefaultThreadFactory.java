package com.taobao.csp.ahas.service.api.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
   private static final AtomicInteger poolNumber = new AtomicInteger(1);
   private final ThreadGroup group;
   private final AtomicInteger threadNumber = new AtomicInteger(1);
   private final String namePrefix;

   public DefaultThreadFactory(String name) {
      SecurityManager s = System.getSecurityManager();
      this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      String newName = name != null ? name : "thread";
      this.namePrefix = "pool-ahas-" + poolNumber.getAndIncrement() + "-" + newName + "-";
   }

   public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(this.group, runnable, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
      if (thread.isDaemon()) {
         thread.setDaemon(false);
      }

      if (thread.getPriority() != 5) {
         thread.setPriority(5);
      }

      thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
         public void uncaughtException(Thread thread, Throwable ex) {
         }
      });
      return thread;
   }
}
