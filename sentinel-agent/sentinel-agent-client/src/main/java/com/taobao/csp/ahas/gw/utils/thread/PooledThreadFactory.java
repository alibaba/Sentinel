package com.taobao.csp.ahas.gw.utils.thread;

public class PooledThreadFactory extends NamedThreadFactory {
   public PooledThreadFactory(String name) {
      super(name);
   }

   public Thread newThread(Runnable r) {
      Thread t = new Thread(this.group, new PooledByteBufRunnable(r), this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
      t.setContextClassLoader(NamedThreadFactory.class.getClassLoader());
      t.setDaemon(false);
      t.setPriority(10);
      return t;
   }

   class PooledByteBufRunnable implements Runnable {
      private Runnable runnable;

      PooledByteBufRunnable(Runnable runnable) {
         this.runnable = runnable;
      }

      public void run() {
         try {
            this.runnable.run();
         } catch (Throwable var2) {
            var2.printStackTrace();
         }

      }
   }
}
