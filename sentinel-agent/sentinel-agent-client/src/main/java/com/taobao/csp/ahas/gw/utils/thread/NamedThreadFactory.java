package com.taobao.csp.ahas.gw.utils.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
   private static final AtomicInteger poolNumber = new AtomicInteger(1);
   protected final AtomicInteger threadNumber;
   protected final ThreadGroup group;
   protected final String namePrefix;
   protected final boolean isDaemon;

   public NamedThreadFactory() {
      this("pool");
   }

   public NamedThreadFactory(String name) {
      this(name, true);
   }

   public NamedThreadFactory(String preffix, boolean daemon) {
      this.threadNumber = new AtomicInteger(1);
      SecurityManager s = System.getSecurityManager();
      this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      this.namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
      this.isDaemon = daemon;
   }

   public Thread newThread(Runnable r) {
      Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
      t.setContextClassLoader(NamedThreadFactory.class.getClassLoader());
      t.setPriority(10);
      t.setDaemon(this.isDaemon);
      return t;
   }
}
